package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * RequestHistory model for tracking request workflow step execution
 */
data class RequestHistory(
    @DocumentId
    val historyId: Int = 0,
    val requestId: Int = 0,
    val stepId: Int = 0,
    val stepName: String? = null,
    val userId: String = "",
    val userName: String? = null,
    val startTime: Date? = null,
    val endTime: Date? = null,
    val status: String = "Pending", // Pending, InProgress, Completed, Rejected
    val note: String? = null,
    val processingTimeHours: Double? = null,
    @ServerTimestamp
    val createdAt: Date? = null
) {
    /**
     * Calculate processing time if both start and end times are available
     */
    fun calculateProcessingTime(): Double? {
        return if (startTime != null && endTime != null) {
            (endTime.time - startTime.time) / (1000.0 * 60 * 60) // Convert to hours
        } else null
    }
}
