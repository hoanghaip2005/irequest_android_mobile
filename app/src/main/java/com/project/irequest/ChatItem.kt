package com.project.irequest

data class ChatItem(
    val chatId: String,
    val name: String,
    val message: String,
    val timestampMillis: Long,
    val unreadCount: Int,
    val avatarResId: Int,
    val receiverId: String? = null,
    val receiverName: String? = null,
    val sharedChatId: String? = null
)