package com.project.irequest.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.irequest.data.models.Notification
import kotlinx.coroutines.tasks.await

/**
 * Firebase Repository for Notification management
 */
class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    private val notificationsCollection = firestore.collection("notifications")
    
    /**
     * Get notifications for user
     */
    suspend fun getUserNotifications(userId: String, limit: Int = 50): Result<List<Notification>> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val notifications = snapshot.toObjects(Notification::class.java)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection
                .document(notificationId)
                .update("isRead", true)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get unread notification count
     */
    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
