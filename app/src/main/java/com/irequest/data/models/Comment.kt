package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Comment model representing Comment from C# backend
 */
data class Comment(
    @DocumentId
    val commentId: Int = 0,
    val content: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val requestId: Int = 0,
    val userId: String = "",
    val userName: String? = null,
    val userAvatar: String? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
)
