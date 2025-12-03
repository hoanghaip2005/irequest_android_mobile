package com.example.irequest.utils

/**
 * Role name constants matching C# backend RoleName
 */
object RoleName {
    const val ADMINISTRATOR = "Administrator"
    const val EDITOR = "Editor"
    const val MEMBER = "Member"
    const val APPROVER = "Approver"
    const val MANAGER = "Manager"
    
    // Roles for laptop request workflow
    const val IT_STAFF = "ITStaff"
    const val IT_MANAGER = "ITManager"
    const val DEPARTMENT_HEAD = "DepartmentHead"
    const val FINANCE_STAFF = "FinanceStaff"
    const val FINANCE_MANAGER = "FinanceManager"
    const val HR_STAFF = "HRStaff"
    const val HR_MANAGER = "HRManager"
    const val PROCUREMENT_STAFF = "ProcurementStaff"
    const val PROCUREMENT_MANAGER = "ProcurementManager"
    
    /**
     * Check if user has specific role
     */
    fun hasRole(userRoles: List<String>, role: String): Boolean {
        return userRoles.contains(role)
    }
    
    /**
     * Check if user has any of the specified roles
     */
    fun hasAnyRole(userRoles: List<String>, vararg roles: String): Boolean {
        return roles.any { userRoles.contains(it) }
    }
    
    /**
     * Check if user has all specified roles
     */
    fun hasAllRoles(userRoles: List<String>, vararg roles: String): Boolean {
        return roles.all { userRoles.contains(it) }
    }
    
    /**
     * Check if user is admin
     */
    fun isAdmin(userRoles: List<String>): Boolean {
        return userRoles.contains(ADMINISTRATOR)
    }
    
    /**
     * Check if user is manager (any type)
     */
    fun isManager(userRoles: List<String>): Boolean {
        return hasAnyRole(
            userRoles,
            MANAGER,
            IT_MANAGER,
            FINANCE_MANAGER,
            HR_MANAGER,
            PROCUREMENT_MANAGER,
            DEPARTMENT_HEAD
        )
    }
}
