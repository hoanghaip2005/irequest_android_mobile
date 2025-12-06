package com.project.irequest.data.models

/**
 * Department model
 */
data class Department(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val managerUserId: String? = null,
    val managerUserName: String? = null,
    val isActive: Boolean = true
)
