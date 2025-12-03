package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Notification model representing Notification from C# backend
 */
data class Notification(
    @DocumentId
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val isRead: Boolean = false,
    val type: String = "", // in_progress, approved, rejected, comment_added, etc.
    val link: String? = null,
    val requestId: Int? = null,
    val userId: String = "",
    val icon: String? = null,
    val actionUrl: String? = null
)
