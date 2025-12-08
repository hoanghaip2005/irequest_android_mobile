package com.example.irequest.data.repository

import com.example.irequest.data.models.UserRole
import com.example.irequest.data.models.WorkflowProcess
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseWorkflowRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val rolesCollection = firestore.collection("user_roles")
    private val workflowsCollection = firestore.collection("workflows")
    
    // Lấy role của user hiện tại
    suspend fun getCurrentUserRole(): Result<UserRole> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            val doc = rolesCollection.document(userId).get().await()
            
            if (doc.exists()) {
                val role = doc.toObject(UserRole::class.java)
                Result.success(role ?: UserRole(userId = userId))
            } else {
                // Tạo role mặc định cho user mới
                val defaultRole = UserRole(
                    userId = userId,
                    email = auth.currentUser?.email ?: "",
                    displayName = auth.currentUser?.displayName ?: "User",
                    role = UserRole.ROLE_USER
                )
                rolesCollection.document(userId).set(defaultRole).await()
                Result.success(defaultRole)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Set admin role cho user (dùng 1 lần để setup)
    suspend fun setAdminRole(userId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Chưa đăng nhập"))
            
            val adminRole = UserRole(
                userId = userId,
                email = auth.currentUser?.email ?: "",
                displayName = auth.currentUser?.displayName ?: "Admin",
                role = UserRole.ROLE_ADMIN,
                permissions = listOf("all"),
                createdAt = com.google.firebase.Timestamp.now()
            )
            
            rolesCollection.document(userId).set(adminRole).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Kiểm tra user có quyền xử lý request không
    suspend fun canProcessRequest(requestId: String, requiredRole: String = UserRole.ROLE_STAFF): Result<Boolean> {
        return try {
            val userRoleResult = getCurrentUserRole()
            if (userRoleResult.isFailure) {
                return Result.success(false)
            }
            
            val userRole = userRoleResult.getOrNull() ?: return Result.success(false)
            val userLevel = UserRole.getRoleLevel(userRole.role)
            val requiredLevel = UserRole.getRoleLevel(requiredRole)
            
            Result.success(userLevel >= requiredLevel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Tạo workflow mặc định
    suspend fun createDefaultWorkflow(): Result<String> {
        return try {
            val defaultWorkflow = WorkflowProcess(
                id = "default",
                name = "Quy trình xử lý yêu cầu chuẩn",
                description = "Quy trình 3 bước: Tiếp nhận -> Xử lý -> Hoàn thành",
                steps = listOf(
                    com.example.irequest.data.models.WorkflowStepConfig(
                        stepOrder = 1,
                        stepName = "Tiếp nhận yêu cầu",
                        requiredRole = UserRole.ROLE_STAFF,
                        actionType = "review",
                        canReassign = true,
                        timeoutHours = 24
                    ),
                    com.example.irequest.data.models.WorkflowStepConfig(
                        stepOrder = 2,
                        stepName = "Xử lý yêu cầu",
                        requiredRole = UserRole.ROLE_STAFF,
                        actionType = "approve",
                        canReassign = true,
                        timeoutHours = 48
                    ),
                    com.example.irequest.data.models.WorkflowStepConfig(
                        stepOrder = 3,
                        stepName = "Hoàn thành",
                        requiredRole = UserRole.ROLE_MANAGER,
                        actionType = "complete",
                        canReassign = false,
                        timeoutHours = 24
                    )
                ),
                isActive = true,
                createdAt = com.google.firebase.Timestamp.now()
            )
            
            workflowsCollection.document("default").set(defaultWorkflow).await()
            Result.success("default")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Lấy workflow
    suspend fun getWorkflow(workflowId: String = "default"): Result<WorkflowProcess> {
        return try {
            val doc = workflowsCollection.document(workflowId).get().await()
            
            if (doc.exists()) {
                val workflow = doc.toObject(WorkflowProcess::class.java)
                Result.success(workflow ?: WorkflowProcess())
            } else {
                Result.failure(Exception("Workflow không tồn tại"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
