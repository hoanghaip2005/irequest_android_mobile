package com.project.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Notification model
 */
data class Notification(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "request", "comment", "approval", "system"
    val userId: String = "",
    val requestId: String? = null,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)
