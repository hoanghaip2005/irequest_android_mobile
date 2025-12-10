package com.example.irequest.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Firebase Repository for Report/Analytics data
 */
class FirebaseReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    /**
     * Kiểm tra xem user có quyền admin không
     */
    private suspend fun isAdmin(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val role = userDoc.getString("role") ?: "user"
            role == "admin"
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Lấy thống kê yêu cầu của user trong tháng hiện tại
     * Nếu là admin, sẽ lấy tất cả yêu cầu của mọi người
     */
    suspend fun getMonthlyRequestStats(userId: String): Result<MonthlyRequestStats> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.time
            
            val isUserAdmin = isAdmin(userId)
            
            // Lấy tất cả requests rồi filter trong bộ nhớ để tránh cần index
            val allSnapshot = if (isUserAdmin) {
                firestore.collection("requests").get().await()
            } else {
                firestore.collection("requests")
                    .whereEqualTo("createdById", userId)
                    .get().await()
            }
            
            // Filter trong bộ nhớ
            val requestsThisMonth = allSnapshot.documents.filter { doc ->
                val createdAt = doc.getDate("createdAt")
                createdAt != null && createdAt >= startOfMonth
            }
            
            val totalCreated = requestsThisMonth.size
            
            val totalCompleted = requestsThisMonth.count { doc ->
                doc.getString("statusName") == "Đã hoàn thành"
            }
            
            val totalProcessing = requestsThisMonth.count { doc ->
                doc.getString("statusName") == "Đang xử lý"
            }
            
            val totalRejected = requestsThisMonth.count { doc ->
                doc.getString("statusName") == "Từ chối"
            }
            
            // Tính điểm hiệu suất (completion rate)
            val completionRate = if (totalCreated > 0) {
                (totalCompleted.toFloat() / totalCreated * 100).toInt()
            } else 0
            
            val stats = MonthlyRequestStats(
                totalCreated = totalCreated,
                totalCompleted = totalCompleted,
                totalProcessing = totalProcessing,
                totalRejected = totalRejected,
                completionRate = completionRate
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy lịch sử hoạt động gần đây của user
     * Nếu là admin, sẽ lấy tất cả hoạt động của mọi người
     */
    suspend fun getRecentActivities(userId: String, limit: Int = 10): Result<List<RequestActivity>> {
        return try {
            val isUserAdmin = isAdmin(userId)
            
            // Lấy tất cả request_history rồi filter + sort trong bộ nhớ
            val allSnapshot = if (isUserAdmin) {
                // Admin lấy tất cả
                firestore.collection("request_history").get().await()
            } else {
                // User thường chỉ lấy của mình
                firestore.collection("request_history")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }
            
            // Lấy tất cả requests để map requestId -> title
            val requestsSnapshot = firestore.collection("requests").get().await()
            val requestTitleMap = requestsSnapshot.documents.associate { doc ->
                doc.id to (doc.getString("title") ?: "Không có tiêu đề")
            }
            
            val activities = allSnapshot.documents.mapNotNull { doc ->
                try {
                    // requestId là String (document ID)
                    val requestIdStr = doc.getString("requestId") ?: ""
                    val requestTitle = requestTitleMap[requestIdStr] ?: "Không có tiêu đề"
                    
                    // Map action sang tiếng Việt
                    val actionStr = doc.getString("action") ?: ""
                    val actionVi = when (actionStr.lowercase()) {
                        "created" -> "Tạo mới"
                        "approved" -> "Phê duyệt"
                        "rejected" -> "Từ chối"
                        "completed" -> "Hoàn thành"
                        "updated" -> "Cập nhật"
                        "processing" -> "Đang xử lý"
                        else -> actionStr
                    }
                    
                    RequestActivity(
                        id = doc.id,
                        requestId = requestIdStr.hashCode(),
                        requestTitle = requestTitle,
                        action = actionVi,
                        statusName = doc.getString("statusName") ?: "",
                        createdAt = doc.getDate("createdAt") ?: Date(),
                        userId = doc.getString("userId") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            // Sort trong bộ nhớ theo createdAt giảm dần, lấy limit items
            .sortedByDescending { it.createdAt }
            .take(limit)
            
            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy thống kê theo loại ưu tiên
     * Nếu là admin, sẽ lấy tất cả yêu cầu
     */
    suspend fun getPriorityStats(userId: String): Result<PriorityStats> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val startOfMonth = calendar.time
            
            val isUserAdmin = isAdmin(userId)
            
            // Lấy tất cả requests rồi filter trong bộ nhớ
            val allSnapshot = if (isUserAdmin) {
                firestore.collection("requests").get().await()
            } else {
                firestore.collection("requests")
                    .whereEqualTo("createdById", userId)
                    .get().await()
            }
            
            // Filter theo tháng hiện tại
            val requestsThisMonth = allSnapshot.documents.filter { doc ->
                val createdAt = doc.getDate("createdAt")
                createdAt != null && createdAt >= startOfMonth
            }
            
            var highPriority = 0
            var mediumPriority = 0
            var lowPriority = 0
            
            requestsThisMonth.forEach { doc ->
                when (doc.getString("priority")) {
                    "Cao", "High" -> highPriority++
                    "Trung bình", "Medium" -> mediumPriority++
                    "Thấp", "Low" -> lowPriority++
                }
            }
            
            val stats = PriorityStats(
                high = highPriority,
                medium = mediumPriority,
                low = lowPriority
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lấy thống kê thời gian xử lý trung bình
     * Nếu là admin, sẽ tính tất cả yêu cầu
     */
    suspend fun getAverageProcessingTime(userId: String): Result<ProcessingTimeStats> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.time
            
            val isUserAdmin = isAdmin(userId)
            
            // Lấy tất cả requests rồi filter trong bộ nhớ
            val allSnapshot = if (isUserAdmin) {
                firestore.collection("requests").get().await()
            } else {
                firestore.collection("requests")
                    .whereEqualTo("createdById", userId)
                    .get().await()
            }
            
            // Filter requests hoàn thành trong tháng hiện tại
            val completedRequests = allSnapshot.documents.filter { doc ->
                val createdAt = doc.getDate("createdAt")
                val statusName = doc.getString("statusName")
                createdAt != null && createdAt >= startOfMonth && statusName == "Đã hoàn thành"
            }
            
            var totalDays = 0L
            var count = 0
            
            completedRequests.forEach { doc ->
                val createdAt = doc.getDate("createdAt")
                val updatedAt = doc.getDate("updatedAt")
                
                if (createdAt != null && updatedAt != null) {
                    val diff = updatedAt.time - createdAt.time
                    val days = diff / (1000 * 60 * 60 * 24)
                    totalDays += days
                    count++
                }
            }
            
            val avgDays = if (count > 0) totalDays / count else 0
            
            val stats = ProcessingTimeStats(
                averageDays = avgDays.toInt(),
                totalCompleted = count
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Data models for Report
 */
data class MonthlyRequestStats(
    val totalCreated: Int = 0,
    val totalCompleted: Int = 0,
    val totalProcessing: Int = 0,
    val totalRejected: Int = 0,
    val completionRate: Int = 0 // Phần trăm hoàn thành
)

data class RequestActivity(
    val id: String = "",
    val requestId: Int = 0,
    val requestTitle: String = "",
    val action: String = "", // "Tạo mới", "Phê duyệt", "Từ chối", "Cập nhật"
    val statusName: String = "",
    val createdAt: Date = Date(),
    val userId: String = ""
)

data class PriorityStats(
    val high: Int = 0,
    val medium: Int = 0,
    val low: Int = 0
)

data class ProcessingTimeStats(
    val averageDays: Int = 0,
    val totalCompleted: Int = 0
)
