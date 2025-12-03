package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * RequestRating model for user feedback
 */
data class RequestRating(
    @DocumentId
    val id: Int = 0,
    val requestId: Int = 0,
    val userId: String = "",
    val rating: Int = 0, // 1-5 stars
    val feedback: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)
