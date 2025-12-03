package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Request model representing Request from C# backend
 */
data class Request(
    @DocumentId
    val id: String = "", // Firestore document ID
    val requestId: Int = 0, // Business ID
    val title: String = "",
    val description: String? = null,
    
    // Attachment properties
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    val attachmentFileType: String? = null,
    val attachmentFileSize: Long? = null,
    
    // Status and approval
    val isApproved: Boolean = false,
    val statusId: Int? = null,
    val statusName: String? = null,
    
    // Priority and workflow
    val priorityId: Int? = null,
    val priorityName: String? = null,
    val workflowId: Int? = null,
    val workflowName: String? = null,
    
    // User relationships
    val userId: String = "",
    val userName: String? = null,
    val userEmail: String? = null,
    val assignedUserId: String? = null,
    val assignedUserName: String? = null,
    
    // Timestamps
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val closedAt: Date? = null,
    val approvedAt: Date? = null,
    
    // Additional tracking
    val departmentId: Int? = null,
    val departmentName: String? = null,
    val rejectionReason: String? = null,
    val completionNotes: String? = null,
    val currentStepOrder: Int = 0,
    val roleId: String = "default",
    
    // UI state
    val isStarred: Boolean = false,
    val isRead: Boolean = false,
    
    // Computed fields (not stored in Firebase)
    val commentCount: Int = 0,
    val attachmentCount: Int = 0,
    val historyCount: Int = 0,
    val starCount: Int = 0,
    val daysOpen: Int? = null,
    val nextAssignee: String? = null,
    val nextStepName: String? = null,
    val remainingSteps: Int = 0
)
