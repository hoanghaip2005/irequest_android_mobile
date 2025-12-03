package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Department model representing Department from C# backend
 */
data class Department(
    @DocumentId
    val departmentId: Int = 0,
    val name: String = "",
    val description: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null,
    val isActive: Boolean = true,
    val assignedUserId: String? = null,
    val assignedUserName: String? = null,
    
    // Computed fields for relationships
    val userCount: Int = 0,
    val requestCount: Int = 0
)
