package com.example.irequest.data.models

/**
 * UI State models for Android app
 */

/**
 * Dashboard statistics
 */
data class DashboardStats(
    val totalRequests: Int = 0,
    val pendingRequests: Int = 0,
    val approvedRequests: Int = 0,
    val rejectedRequests: Int = 0,
    val inProgressRequests: Int = 0,
    val completedRequests: Int = 0,
    val myTasksCount: Int = 0,
    val starredCount: Int = 0,
    val unreadNotifications: Int = 0
)

/**
 * Filter options for requests
 */
data class RequestFilter(
    val statusIds: List<Int>? = null,
    val priorityIds: List<Int>? = null,
    val departmentIds: List<Int>? = null,
    val workflowIds: List<Int>? = null,
    val assignedToMe: Boolean = false,
    val createdByMe: Boolean = false,
    val starredOnly: Boolean = false,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val searchQuery: String? = null,
    val sortBy: String = "createdAt", // createdAt, updatedAt, priority, status
    val sortOrder: String = "desc" // asc, desc
)

/**
 * UI State for request list
 */
data class RequestListState(
    val isLoading: Boolean = false,
    val requests: List<Request> = emptyList(),
    val error: String? = null,
    val hasMore: Boolean = false,
    val currentPage: Int = 1
)

/**
 * UI State for request details
 */
data class RequestDetailState(
    val isLoading: Boolean = false,
    val request: Request? = null,
    val comments: List<Comment> = emptyList(),
    val history: List<RequestHistory> = emptyList(),
    val error: String? = null
)

/**
 * UI State for authentication
 */
data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

/**
 * UI State for notifications
 */
data class NotificationState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)
