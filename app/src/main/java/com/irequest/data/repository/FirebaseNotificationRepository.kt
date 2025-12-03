package com.example.irequest.data.repository

import com.example.irequest.data.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Repository for Notifications
 */
class FirebaseNotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val notificationsCollection = firestore.collection("notifications")
    
    /**
     * Get notifications for current user
     */
    suspend fun getNotifications(limit: Int = 50): Result<List<Notification>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
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
     * Get unread notifications count
     */
    suspend fun getUnreadCount(): Result<Int> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
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
    
    /**
     * Mark notification as read
     */
    suspend fun markAsRead(notificationId: Int): Result<Unit> {
        return try {
            notificationsCollection
                .document(notificationId.toString())
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
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
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
     * Delete notification
     */
    suspend fun deleteNotification(notificationId: Int): Result<Unit> {
        return try {
            notificationsCollection
                .document(notificationId.toString())
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Listen to notifications in real-time
     */
    fun observeNotifications(): Flow<List<Notification>> = callbackFlow {
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            close()
            return@callbackFlow
        }
        
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val notifications = snapshot.toObjects(Notification::class.java)
                    trySend(notifications)
                }
            }
        
        awaitClose { listener.remove() }
    }
}
