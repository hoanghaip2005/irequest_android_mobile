package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Chat model for direct messages and group chats
 */
data class Chat(
    @DocumentId
    val id: String = "",
    // "user" or "group"
    val type: String = "",
    val userId: String = "",
    val userName: String? = null,
    val groupName: String? = null,
    val createdById: String = "",
    val createdByName: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    
    // For direct user chats - info about the other person
    val otherUserId: String? = null,
    val otherUserName: String? = null,
    
    // Shared chat ID to link conversations between 2 users
    val sharedChatId: String? = null,
    
    // Additional UI fields
    val lastMessage: String? = null,
    val lastMessageTime: Date? = null,
    val unreadCount: Int = 0,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false
)

/**
 * Message model for chat messages
 */
data class Message(
    @DocumentId
    val id: String = "",
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    
    // Sender info
    val senderId: String = "",
    val senderName: String? = null,
    val senderAvatar: String? = null,
    
    // Receiver info (for direct messages)
    val receiverId: String? = null,
    val receiverName: String? = null,
    
    // Group info (for group messages)
    val groupId: String? = null,
    val groupName: String? = null,
    
    // Message status
    val isRead: Boolean = false,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val editedAt: Date? = null,
    val deletedAt: Date? = null,
    val pinnedAt: Date? = null,
    
    // Attachment info
    // "image", "video", "file", "audio"
    val attachmentType: String? = null,
    val originalFileName: String? = null,
    val attachmentPath: String? = null,
    val attachmentSize: Long? = null,
    val thumbnailUrl: String? = null,
    
    // Image message (simplified)
    val imageUrl: String? = null,
    val messageType: String? = null, // "TEXT", "IMAGE", "TEXT_WITH_IMAGE"
    
    // Reply to message
    val replyToMessageId: String? = null,
    val replyToContent: String? = null
)

/**
 * ChatGroup model
 */
data class ChatGroup(
    @DocumentId
    val id: String = "",
    val groupName: String = "",
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdById: String = "",
    val createdByName: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val memberIds: List<String> = emptyList(),
    val adminIds: List<String> = emptyList(),
    val isActive: Boolean = true,
    
    // Group settings
    val maxMembers: Int = 100,
    val isPublic: Boolean = false,
    val allowMemberInvite: Boolean = true,
    
    // Statistics
    val totalMessages: Int = 0,
    val lastMessageTime: Date? = null
)

/**
 * ChatGroupMember model
 */
data class ChatGroupMember(
    @DocumentId
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val userName: String? = null,
    val userAvatar: String? = null,
    val isAdmin: Boolean = false,
    // Role: "admin" or "member"
    val role: String = "member",
    @ServerTimestamp
    val joinedAt: Date? = null,
    val addedById: String? = null,
    val addedByName: String? = null,
    val isActive: Boolean = true,
    val isMuted: Boolean = false,
    val lastReadMessageId: String? = null,
    val lastReadAt: Date? = null
)
