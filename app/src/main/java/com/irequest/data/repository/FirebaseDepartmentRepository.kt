package com.example.irequest.data.repository

import com.example.irequest.data.models.Department
import com.example.irequest.data.models.Employee
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseDepartmentRepository {
    
    private val db = FirebaseFirestore.getInstance()
    private val departmentsCollection = db.collection("departments")
    private val usersCollection = db.collection("users")
    
    /**
     * Lấy tất cả phòng ban từ Firestore
     */
    suspend fun getAllDepartments(): Result<List<Department>> {
        return try {
            val snapshot = departmentsCollection
                .get()
                .await()
            
            val departments = snapshot.documents.mapNotNull { doc ->
                try {
                    val dept = doc.toObject(Department::class.java) ?: return@mapNotNull null
                    
                    // Load employees for this department
                    val employees = getEmployeesByDepartmentId(dept.departmentId).getOrNull() ?: emptyList()
                    
                    dept.copy(
                        employees = employees,
                        userCount = employees.size
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            
            Result.success(departments)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Lấy danh sách nhân viên theo phòng ban
     */
    private suspend fun getEmployeesByDepartmentId(departmentId: Int): Result<List<Employee>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("departmentId", departmentId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val employees = snapshot.documents.mapNotNull { doc ->
                try {
                    val name = doc.getString("displayName") ?: doc.getString("email") ?: "Unknown"
                    val role = doc.getString("role") ?: "Nhân viên"
                    val position = doc.getString("position") ?: ""
                    
                    // Hiển thị chức vụ nếu có, không thì hiển thị role
                    val displayRole = position.ifEmpty { 
                        when (role) {
                            "admin" -> "Quản trị viên"
                            "manager" -> "Quản lý"
                            "staff" -> "Nhân viên"
                            else -> "Người dùng"
                        }
                    }
                    
                    Employee(name = name, role = displayRole)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            
            Result.success(employees)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Tìm kiếm phòng ban theo tên
     */
    suspend fun searchDepartments(query: String): Result<List<Department>> {
        return try {
            val allDepartments = getAllDepartments().getOrNull() ?: emptyList()
            
            val filtered = allDepartments.filter { dept ->
                dept.name.contains(query, ignoreCase = true) ||
                dept.assignedUserName?.contains(query, ignoreCase = true) == true ||
                dept.employees.any { emp -> 
                    emp.name.contains(query, ignoreCase = true)
                }
            }
            
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Tạo phòng ban mới (dành cho admin)
     */
    suspend fun createDepartment(department: Department): Result<String> {
        return try {
            val docRef = departmentsCollection.document()
            docRef.set(department).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
