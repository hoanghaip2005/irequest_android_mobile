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
     */
    suspend fun getOrCreateDirectChat(otherUserId: String, otherUserName: String): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: "User"
            
            // Check if chat already exists
            val existingChat = chatsCollection
                .whereEqualTo("type", "user")
                .whereEqualTo("userId", otherUserId)
                .whereEqualTo("createdById", currentUserId)
                .limit(1)
                .get()
                .await()
            
            if (!existingChat.isEmpty) {
                return Result.success(existingChat.documents[0].id)
            }
            
            // Create new chat
            val chat = Chat(
                type = "user",
                userId = otherUserId,
                userName = otherUserName,
                createdById = currentUserId,
                createdByName = currentUserName,
                createdAt = Date(),
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0
            )
            
            val docRef = chatsCollection.add(chat).await()
            Result.success(docRef.id)
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
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .limitToLast(limit.toLong())
                .get()
                .await()
            
            val messages = snapshot.toObjects(Message::class.java)
            Result.success(messages)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Send message to chat
     */
    suspend fun sendMessage(
        chatId: String,
        content: String,
        receiverId: String? = null,
        receiverName: String? = null
    ): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            val currentUserName = auth.currentUser?.displayName ?: "User"
            
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
            
            // Update chat last message
            chatsCollection.document(chatId).update(
                mapOf(
                    "lastMessage" to content,
                    "lastMessageTime" to Date()
                )
            ).await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
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
     */
    suspend fun markChatMessagesAsRead(chatId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            val snapshot = messagesCollection
                .whereEqualTo("groupId", chatId)
                .whereEqualTo("isRead", false)
                .whereNotEqualTo("senderId", currentUserId)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
            
            // Reset unread count in chat
            chatsCollection.document(chatId).update("unreadCount", 0).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
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
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
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
