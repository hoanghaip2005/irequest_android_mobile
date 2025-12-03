package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Status model representing Status from C# backend
 */
data class Status(
    @DocumentId
    val statusId: Int = 0,
    val statusName: String = "",
    val description: String = "",
    val isFinal: Boolean = false,
    val color: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    
    // Computed fields for relationships
    val requestCount: Int = 0
)
