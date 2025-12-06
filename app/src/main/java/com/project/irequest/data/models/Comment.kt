package com.project.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Comment model
 */
data class Comment(
    @DocumentId
    val id: String = "",
    val requestId: String = "",
    val userId: String = "",
    val userName: String? = null,
    val userAvatar: String? = null,
    val content: String = "",
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
