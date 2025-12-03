package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId

/**
 * WorkflowStep model representing WorkflowStep from C# backend
 */
data class WorkflowStep(
    @DocumentId
    val stepId: Int = 0,
    val stepName: String = "",
    val workflowId: Int? = null,
    val stepOrder: Int = 0,
    val assignedUserId: String? = null,
    val assignedUserName: String? = null,
    val requiredRoleId: String? = null,
    val requiredRoleName: String? = null,
    val timeLimitHours: Int? = null,
    val departmentId: Int? = null,
    val departmentName: String? = null,
    val statusId: Int? = null,
    val statusName: String? = null,
    val requiresApproval: Boolean = false,
    val canReject: Boolean = true,
    val canDelegate: Boolean = false,
    val notifyAssignee: Boolean = true,
    val allowComments: Boolean = true,
    val formFields: String? = null // JSON for additional form fields
)
