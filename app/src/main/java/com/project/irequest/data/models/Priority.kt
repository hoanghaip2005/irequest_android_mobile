package com.project.irequest.data.models

/**
 * Priority model
 */
data class Priority(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val color: String = "#000000",
    val level: Int = 0, // 1=Low, 2=Medium, 3=High, 4=Urgent
    val isActive: Boolean = true
)

/**
 * Common priorities
 */
object RequestPriority {
    const val LOW = 1
    const val MEDIUM = 2
    const val HIGH = 3
    const val URGENT = 4
}
