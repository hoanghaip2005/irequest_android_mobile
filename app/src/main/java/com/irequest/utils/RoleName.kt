package com.irequest.utils

/**
 * RoleName constants cho hệ thống quản lý yêu cầu (Request Management System)
 * Phù hợp với các vai trò chính trong đề tài
 */
object RoleName {
    // Vai trò chính
    const val USER = "User"                    // Người yêu cầu (Requester)
    const val AGENT = "Agent"                  // Người xử lý (Support staff)
    const val APPROVER = "Approver"            // Người phê duyệt
    const val ADMIN = "Admin"                  // Quản trị viên

    // Các hàm tiện ích
    fun hasRole(userRoles: List<String>, role: String): Boolean {
        return userRoles.contains(role)
    }

    fun hasAnyRole(userRoles: List<String>, vararg roles: String): Boolean {
        return roles.any { userRoles.contains(it) }
    }

    fun hasAllRoles(userRoles: List<String>, vararg roles: String): Boolean {
        return roles.all { userRoles.contains(it) }
    }

    fun isAdmin(userRoles: List<String>): Boolean {
        return userRoles.contains(ADMIN)
    }

    fun isAgent(userRoles: List<String>): Boolean {
        return userRoles.contains(AGENT)
    }

    fun isApprover(userRoles: List<String>): Boolean {
        return userRoles.contains(APPROVER)
    }

    fun isUser(userRoles: List<String>): Boolean {
        return userRoles.contains(USER)
    }
}
