package com.project.irequest

data class ChatItem(
    val name: String,
    val message: String,
    val timestampMillis: Long,
    val unreadCount: Int,
    val avatarResId: Int
)