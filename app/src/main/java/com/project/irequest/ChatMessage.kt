package com.project.irequest

data class ChatMessage(
    val text: String,
    val isSent: Boolean, // true nếu là tin nhắn đã gửi, false nếu là tin nhắn đã nhận
    val imageUrl: String? = null, // URL của ảnh nếu có
    val messageType: MessageType = MessageType.TEXT // Loại tin nhắn
)

enum class MessageType {
    TEXT,
    IMAGE,
    TEXT_WITH_IMAGE
}