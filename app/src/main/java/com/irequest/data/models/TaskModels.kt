package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * PersonalTask model for personal task scheduling
 */
data class PersonalTask(
    @DocumentId
    val taskId: Int = 0,
    val taskName: String = "",
    val description: String? = null,
    val startTime: Date? = null,
    val endTime: Date? = null,
    val priority: Int = 3, // 1-5 (1 = highest, 5 = lowest)
    val status: String = "pending", // pending, in_progress, completed, cancelled
    val userId: String = "",
    val userName: String? = null,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val tags: List<String> = emptyList(),
    val prerequisites: List<Int> = emptyList(), // Task IDs that must be completed first
    val estimatedHours: Double? = null,
    val actualHours: Double? = null,
    val completedAt: Date? = null,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null, // daily, weekly, monthly, yearly
    val reminderTime: Date? = null,
    val notifyBefore: Int? = null, // minutes
    @ServerTimestamp
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)

/**
 * SubWorkflowExecution model for tracking sub-workflow execution
 */
data class SubWorkflowExecution(
    @DocumentId
    val id: String = "",
    val parentRequestId: Int = 0,
    val parentStepId: Int = 0,
    val subWorkflowId: Int = 0,
    val subWorkflowName: String? = null,
    val subRequestId: Int? = null,
    val status: String = "pending", // pending, in_progress, completed, failed
    val startedAt: Date? = null,
    val completedAt: Date? = null,
    val startedById: String? = null,
    val startedByName: String? = null,
    val result: String? = null,
    val errorMessage: String? = null,
    @ServerTimestamp
    val createdAt: Date? = null
)

/**
 * WorkflowDependency model for workflow dependencies
 */
data class WorkflowDependency(
    @DocumentId
    val id: String = "",
    val workflowId: Int = 0,
    val dependsOnWorkflowId: Int = 0,
    val dependsOnWorkflowName: String? = null,
    val dependencyType: String = "required", // required, optional, conditional
    val condition: String? = null, // JSON condition for conditional dependencies
    val description: String? = null,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null
)

/**
 * RequestStepHistory model for tracking request step execution history
 */
data class RequestStepHistory(
    @DocumentId
    val id: String = "",
    val requestId: Int = 0,
    val stepId: Int = 0,
    val stepName: String? = null,
    val stepOrder: Int = 0,
    val action: String = "", // assigned, started, approved, rejected, completed, delegated, commented
    val actionById: String = "",
    val actionByName: String? = null,
    val previousAssigneeId: String? = null,
    val newAssigneeId: String? = null,
    val previousStatusId: Int? = null,
    val newStatusId: Int? = null,
    val statusName: String? = null,
    val comment: String? = null,
    val timeSpentMinutes: Int? = null,
    val isAutomatic: Boolean = false,
    @ServerTimestamp
    val actionAt: Date? = null,
    val metadata: String? = null // JSON for additional data
)
