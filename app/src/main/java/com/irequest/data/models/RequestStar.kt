package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * RequestStar model for bookmarking/starring requests
 */
data class RequestStar(
    @DocumentId
    val id: String = "",
    val requestId: Int = 0,
    val userId: String = "",
    @ServerTimestamp
    val starredAt: Date? = null
)
