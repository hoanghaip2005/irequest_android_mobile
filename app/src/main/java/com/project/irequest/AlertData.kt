package com.project.irequest

import java.util.UUID

enum class AlertType {
    REQUEST_UPDATE, REQUEST_APPROVED, REQUEST_REJECTED,
    COMMENT_ADDED, TASK_ASSIGNED, CHAT_MESSAGE, SLA_WARNING, INFO
}

data class AlertData(
    val id: String = UUID.randomUUID().toString(),
    val type: AlertType,
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean, // Đổi thành var để update trực tiếp
    val group: String,   // "Today", "Yesterday"
    val badgeText: String? = null
)