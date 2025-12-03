package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * User model representing AppUser from C# backend
 */
data class User(
    @DocumentId
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val avatar: String? = null,
    val birthDate: Date? = null,
    val departmentId: Int? = null,
    val departmentName: String? = null,
    val emailConfirmed: Boolean = false,
    val phoneNumberConfirmed: Boolean = false,
    val roles: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    
    // Computed fields for relationships (not stored in Firebase)
    val requestCount: Int = 0,
    val assignedRequestCount: Int = 0,
    val commentCount: Int = 0
)
