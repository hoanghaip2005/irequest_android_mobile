package com.example.irequest.data.models.dashboard

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Dashboard statistics and analytics models
 */

/**
 * Dashboard summary statistics
 */
data class DashboardStats(
    val totalRequests: Int = 0,
    val pendingRequests: Int = 0,
    val inProgressRequests: Int = 0,
    val completedRequests: Int = 0,
    val rejectedRequests: Int = 0,
    val myRequestsCount: Int = 0,
    val assignedToMeCount: Int = 0,
    val overdueCount: Int = 0,
    val todayNewRequests: Int = 0,
    val averageCompletionTime: Long = 0 // in hours
)

/**
 * Detailed dashboard view model
 */
data class DashboardDetail(
    val stats: DashboardStats,
    val recentRequests: List<com.example.irequest.data.models.Request> = emptyList(),
    val pendingApprovals: List<com.example.irequest.data.models.Request> = emptyList(),
    val upcomingDeadlines: List<RequestDeadline> = emptyList(),
    val activityFeed: List<ActivityItem> = emptyList(),
    @ServerTimestamp
    val lastUpdated: Date? = null
)

/**
 * Request deadline info
 */
data class RequestDeadline(
    val requestId: Int = 0,
    val title: String = "",
    val dueDate: Date? = null,
    val hoursRemaining: Long = 0,
    val isOverdue: Boolean = false,
    val priorityName: String = "",
    val priorityColor: String = ""
)

/**
 * Activity feed item
 */
data class ActivityItem(
    val id: String = "",
    val type: String = "", // "created", "updated", "commented", "approved", "rejected"
    val title: String = "",
    val description: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val requestId: Int? = null,
    val icon: String = "",
    val color: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

/**
 * Request statistics by category
 */
data class RequestStatsByCategory(
    val categoryName: String = "",
    val totalRequests: Int = 0,
    val pendingCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val rejectedCount: Int = 0,
    val averageCompletionTime: Long = 0, // in hours
    val percentage: Float = 0f
)

/**
 * Request statistics by priority
 */
data class RequestStatsByPriority(
    val priorityId: Int = 0,
    val priorityName: String = "",
    val totalRequests: Int = 0,
    val onTimeCount: Int = 0,
    val overdueCount: Int = 0,
    val averageResponseTime: Long = 0, // in minutes
    val color: String = ""
)

/**
 * Request statistics by department
 */
data class RequestStatsByDepartment(
    val departmentId: Int = 0,
    val departmentName: String = "",
    val totalRequests: Int = 0,
    val assignedToMe: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val averageCompletionTime: Long = 0 // in hours
)

/**
 * Weekly/Monthly statistics
 */
data class PeriodStatistics(
    val period: String = "", // "week", "month", "quarter", "year"
    val startDate: Date? = null,
    val endDate: Date? = null,
    val totalRequests: Int = 0,
    val completedRequests: Int = 0,
    val rejectedRequests: Int = 0,
    val averageCompletionTime: Long = 0, // in hours
    val satisfactionRate: Float = 0f, // 0.0 to 1.0
    val dailyBreakdown: List<DailyStats> = emptyList()
)

/**
 * Daily statistics breakdown
 */
data class DailyStats(
    val date: Date? = null,
    val totalRequests: Int = 0,
    val completedRequests: Int = 0,
    val newRequests: Int = 0
)

/**
 * User performance metrics
 */
data class UserPerformance(
    val userId: String = "",
    val userName: String = "",
    val totalRequestsHandled: Int = 0,
    val completedCount: Int = 0,
    val averageCompletionTime: Long = 0, // in hours
    val averageRating: Float = 0f, // 0.0 to 5.0
    val onTimeCompletionRate: Float = 0f, // percentage
    val currentWorkload: Int = 0 // active requests assigned
)

/**
 * SLA Compliance metrics
 */
data class SLACompliance(
    val totalRequests: Int = 0,
    val onTimeCount: Int = 0,
    val overdueCount: Int = 0,
    val complianceRate: Float = 0f, // percentage
    val averageResponseTime: Long = 0, // in minutes
    val averageResolutionTime: Long = 0, // in minutes
    val byPriority: List<PrioritySLA> = emptyList()
)

/**
 * SLA metrics by priority
 */
data class PrioritySLA(
    val priorityId: Int = 0,
    val priorityName: String = "",
    val targetResponseTime: Int = 0, // in minutes
    val targetResolutionTime: Int = 0, // in minutes
    val actualResponseTime: Long = 0, // in minutes
    val actualResolutionTime: Long = 0, // in minutes
    val complianceRate: Float = 0f // percentage
)
