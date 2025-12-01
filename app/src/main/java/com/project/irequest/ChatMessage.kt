package com.project.irequest

data class ChatMessage(
    val text: String,
    val isSent: Boolean // true nếu là tin nhắn đã gửi, false nếu là tin nhắn đã nhận
)