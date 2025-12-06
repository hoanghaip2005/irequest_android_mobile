package com.project.irequest.data.models

/**
 * Status model
 */
data class Status(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val color: String = "#000000",
    val order: Int = 0,
    val isActive: Boolean = true
)

/**
 * Common statuses
 */
object RequestStatus {
    const val NEW = 1
    const val IN_PROGRESS = 2
    const val PENDING_APPROVAL = 3
    const val APPROVED = 4
    const val REJECTED = 5
    const val COMPLETED = 6
    const val CANCELLED = 7
}
