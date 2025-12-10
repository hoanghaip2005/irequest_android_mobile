package com.example.irequest.data.repository

import com.example.irequest.data.models.Chat
import com.example.irequest.data.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Firebase Repository for Chat and Messages
 */
class FirebaseChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    
    private val chatsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")
    
    /**
     * Get all chats for current user
     */
    suspend fun getUserChats(): Result<List<Chat>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            // Get chats without compound index - sort in memory
            val snapshot = chatsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val chats = snapshot.toObjects(Chat::class.java)
                .sortedByDescending { it.lastMessageTime }
            
            Result.success(chats)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Get chat by ID
     */
    suspend fun getChatById(chatId: String): Result<Chat?> {
        return try {
            val snapshot = chatsCollection
                .document(chatId)
                .get()
                .await()
            
            val chat = snapshot.toObject(Chat::class.java)
            Result.success(chat)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Create or get direct chat with another user
     * Creates bidirectional chat entries so both users can see the conversation
     */
    suspend fun getOrCreateDirectChat(otherUserId: String, otherUserName: String): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "User"
            
            // Check if chat already exists for current user
            val existingChatForCurrentUser = chatsCollection
                .whereEqualTo("type", "user")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("otherUserId", otherUserId)
                .limit(1)
                .get()
                .await()
            
            if (!existingChatForCurrentUser.isEmpty) {
                return Result.success(existingChatForCurrentUser.documents[0].id)
            }
            
            // Generate a shared chat ID for both users
            val sharedChatId = if (currentUserId < otherUserId) {
                "chat_${currentUserId}_${otherUserId}"
            } else {
                "chat_${otherUserId}_${currentUserId}"
            }
            
            // Create chat entry for current user
            val chatForCurrentUser = Chat(
                type = "user",
                userId = currentUserId,
                userName = currentUserName,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                createdById = currentUserId,
                createdByName = currentUserName,
                createdAt = Date(),
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0,
                sharedChatId = sharedChatId
            )
            
            // Create chat entry for other user
            val chatForOtherUser = Chat(
                type = "user",
                userId = otherUserId,
                userName = otherUserName,
                otherUserId = currentUserId,
                otherUserName = currentUserName,
                createdById = currentUserId,
                createdByName = currentUserName,
                createdAt = Date(),
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0,
                sharedChatId = sharedChatId
            )
            
            // Add both chat entries
            val docRef1 = chatsCollection.add(chatForCurrentUser).await()
            chatsCollection.add(chatForOtherUser).await()
            
            Result.success(docRef1.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Get messages for a chat
     */
    suspend fun getChatMessages(chatId: String, limit: Int = 50): Result<List<Message>> {
        return try {
            val snapshot = messagesCollection
                .whereEqualTo("groupId", chatId)
                // Removed orderBy and limitToLast to avoid index requirement
                .get()
                .await()
            
            val messages = snapshot.toObjects(Message::class.java)
                // Sort by createdAt in memory and take last N messages
                .sortedBy { it.createdAt?.time ?: 0L }
                .takeLast(limit)
            Result.success(messages)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Send message to chat
     * Updates both sender and receiver chat entries
     */
    suspend fun sendMessage(
        chatId: String,
        content: String,
        receiverId: String? = null,
        receiverName: String? = null
    ): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "User"
            
            android.util.Log.d("ChatRepo", "Creating message - chatId: $chatId, sender: $currentUserId, receiver: $receiverId")
            
            val message = Message(
                content = content,
                senderId = currentUserId,
                senderName = currentUserName,
                receiverId = receiverId,
                receiverName = receiverName,
                groupId = chatId,
                createdAt = Date(),
                isRead = false
            )
            
            // Add message
            val docRef = messagesCollection.add(message).await()
            android.util.Log.d("ChatRepo", "Message added to Firestore: ${docRef.id}")
            
            val now = Date()
            val senderUpdates = mapOf(
                "lastMessage" to content,
                "lastMessageTime" to now
            )
            
            // Update current user's chat entry (người gửi không cần tăng unreadCount)
            try {
                chatsCollection.document(chatId).update(senderUpdates).await()
                android.util.Log.d("ChatRepo", "Updated sender's chat: $chatId")
            } catch (e: Exception) {
                android.util.Log.w("ChatRepo", "Failed to update sender's chat: ${e.message}")
            }
            
            // Update the other user's chat entry (người nhận cần tăng unreadCount)
            if (receiverId != null) {
                try {
                    val receiverChats = chatsCollection
                        .whereEqualTo("userId", receiverId)
                        .whereEqualTo("otherUserId", currentUserId)
                        .limit(1)
                        .get()
                        .await()
                    
                    if (!receiverChats.isEmpty) {
                        val receiverChatDoc = receiverChats.documents[0]
                        val receiverChatId = receiverChatDoc.id
                        val currentUnreadCount = receiverChatDoc.getLong("unreadCount") ?: 0
                        
                        val receiverUpdates = mapOf(
                            "lastMessage" to content,
                            "lastMessageTime" to now,
                            "unreadCount" to (currentUnreadCount + 1)
                        )
                        
                        chatsCollection.document(receiverChatId).update(receiverUpdates).await()
                        android.util.Log.d("ChatRepo", "Updated receiver's chat: $receiverChatId with unreadCount: ${currentUnreadCount + 1}")
                    } else {
                        android.util.Log.w("ChatRepo", "Receiver's chat not found for userId: $receiverId")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("ChatRepo", "Failed to update receiver's chat: ${e.message}")
                }
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepo", "Error sending message", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Mark message as read
     */
    suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            messagesCollection.document(messageId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Mark all messages in chat as read
     * Simplified query to avoid composite index requirement
     */
    suspend fun markChatMessagesAsRead(chatId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            android.util.Log.d("ChatRepo", "Marking messages as read for groupId: $chatId, currentUser: $currentUserId")
            
            // Get all unread messages in this chat (filter senderId in memory)
            val snapshot = messagesCollection
                .whereEqualTo("groupId", chatId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            var updateCount = 0
            snapshot.documents.forEach { doc ->
                val senderId = doc.getString("senderId")
                // Only mark as read if not sent by current user
                if (senderId != currentUserId) {
                    batch.update(doc.reference, "isRead", true)
                    updateCount++
                }
            }
            
            if (updateCount > 0) {
                batch.commit().await()
                android.util.Log.d("ChatRepo", "Marked $updateCount messages as read")
            }
            
            // Reset unread count in current user's chat document
            // Need to find the chat document for current user
            val currentUserChatSnapshot = chatsCollection
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("sharedChatId", chatId)
                .limit(1)
                .get()
                .await()
            
            if (!currentUserChatSnapshot.isEmpty) {
                val chatDocId = currentUserChatSnapshot.documents[0].id
                chatsCollection.document(chatDocId).update("unreadCount", 0).await()
                android.util.Log.d("ChatRepo", "Reset unreadCount for chat: $chatDocId")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ChatRepo", "Error marking messages as read", e)
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Listen to messages in real-time
     */
    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("groupId", chatId)
            // Removed orderBy to avoid index requirement - will sort in memory
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                        // Sort by createdAt in memory
                        .sortedBy { it.createdAt?.time ?: 0L }
                    trySend(messages)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Listen to chats in real-time
     */
    fun observeChats(): Flow<List<Chat>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            close()
            return@callbackFlow
        }
        
        // Listen without compound index - sort in memory
        val listener = chatsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                        .sortedByDescending { it.lastMessageTime }
                    trySend(chats)
                }
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Delete message
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            messagesCollection.document(messageId).update(
                mapOf(
                    "isDeleted" to true,
                    "deletedAt" to Date()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
