package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * RequestApproval model - Tracking who approved/rejected requests
 */
data class RequestApproval(
    @DocumentId
    val id: Int = 0,
    val requestId: Int = 0,
    val approvedByUserId: String = "",
    val approvedByUserName: String? = null,
    @ServerTimestamp
    val approvedAt: Date? = null,
    val note: String? = null,
    val action: String = "", // "approved", "rejected"
    val stepId: Int? = null,
    val stepName: String? = null
)

/**
 * RequestView model - Tracking who viewed requests
 */
data class RequestView(
    @DocumentId
    val requestViewId: Int = 0,
    val requestId: Int = 0,
    val userId: String = "",
    val userName: String? = null,
    @ServerTimestamp
    val viewedAt: Date? = null,
    val viewDuration: Long? = null // in seconds
)

/**
 * ProcessStepRequest model - Request processing through workflow steps
 */
data class ProcessStepRequest(
    @DocumentId
    val id: Int = 0,
    val requestId: Int = 0,
    val stepId: Int = 0,
    val stepName: String = "",
    val stepOrder: Int = 0,
    val assignedUserId: String? = null,
    val assignedUserName: String? = null,
    val statusId: Int = 0,
    val statusName: String = "",
    @ServerTimestamp
    val startedAt: Date? = null,
    val completedAt: Date? = null,
    val dueDate: Date? = null,
    val isCompleted: Boolean = false,
    val notes: String? = null,
    val actionTaken: String? = null // "approved", "rejected", "forwarded"
)

/**
 * RequestWorkflow model - Assigned workflow for a request
 */
data class RequestWorkflow(
    @DocumentId
    val id: Int = 0,
    val requestId: Int = 0,
    val workflowId: Int = 0,
    val workflowName: String = "",
    val currentStepId: Int = 0,
    val currentStepName: String = "",
    @ServerTimestamp
    val startedAt: Date? = null,
    val completedAt: Date? = null,
    val isCompleted: Boolean = false,
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val progress: Float = 0f // 0.0 to 1.0
)

/**
 * AdditionalInfoModel - Additional dynamic fields for requests
 */
data class AdditionalInfoModel(
    @DocumentId
    val id: Int = 0,
    val requestId: Int = 0,
    val fieldName: String = "",
    val fieldValue: String = "",
    val fieldType: String = "", // "text", "number", "date", "boolean", "file"
    val displayOrder: Int = 0,
    @ServerTimestamp
    val createdAt: Date? = null
)

/**
 * Roles model - User roles
 */
data class Role(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val displayName: String = "",
    val description: String? = null,
    val permissions: List<String> = emptyList(),
    val isSystemRole: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null
)
