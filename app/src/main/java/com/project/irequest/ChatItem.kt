package com.project.irequest

data class ChatItem(
    val name: String,
    val message: String,
    val timestampMillis: Long, // Đã thay đổi từ String sang Long
    val unreadCount: Int,
    val avatarResId: Int
)