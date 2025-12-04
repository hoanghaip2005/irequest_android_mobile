package com.project.irequest

data class ChatMessage(
    val content: String, // Text hoặc URL ảnh
    val isSentByMe: Boolean, // true nếu là tin nhắn đã gửi, false nếu là tin nhắn đã nhận
    val isImage: Boolean = false, // true nếu là ảnh
    val imagePath: String? = null, // Đường dẫn ảnh trong Firebase Storage
    val fileName: String? = null, // Tên file gốc
    val timestamp: Long = System.currentTimeMillis()
) {
    // Backward compatibility constructor
    constructor(text: String, isSent: Boolean) : this(
        content = text,
        isSentByMe = isSent,
        isImage = false
    )
}