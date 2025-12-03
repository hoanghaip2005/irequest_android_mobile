package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Workflow model representing Workflow from C# backend
 */
data class Workflow(
    @DocumentId
    val workflowId: Int = 0,
    val workflowName: String = "",
    val priorityId: Int? = null,
    val description: String? = null,
    val isActive: Boolean = true,
    val formSchema: String? = null, // JSON schema for dynamic forms
    @ServerTimestamp
    val createdAt: Date? = null,
    val departmentId: Int? = null,
    
    // Computed fields for relationships
    val stepCount: Int = 0,
    val requestCount: Int = 0
)
