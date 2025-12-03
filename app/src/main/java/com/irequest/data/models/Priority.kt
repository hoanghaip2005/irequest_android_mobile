package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId

/**
 * Priority model representing Priority from C# backend
 */
data class Priority(
    @DocumentId
    val priorityId: Int = 0,
    val priorityName: String = "",
    val sortOrder: Int = 0,
    
    // SLA Core Metrics
    val firstResponseTime: Int? = null, // in minutes
    val resolutionTime: Int? = null, // in minutes
    val responseTimeBetweenSteps: Int? = null, // in minutes
    
    // Display Properties
    val color: String? = null,
    val icon: String? = null,
    val description: String? = null,
    
    // Computed fields for relationships
    val requestCount: Int = 0
)
