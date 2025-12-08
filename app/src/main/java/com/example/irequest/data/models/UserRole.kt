package com.example.irequest.data.models

data class UserRole(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "user", // user, staff, manager, admin
    val department: String = "",
    val permissions: List<String> = emptyList(),
    val createdAt: com.google.firebase.Timestamp? = null
) {
    companion object {
        const val ROLE_USER = "user"
        const val ROLE_STAFF = "staff"
        const val ROLE_MANAGER = "manager"
        const val ROLE_ADMIN = "admin"
        
        fun getRoleLevel(role: String): Int {
            return when(role) {
                ROLE_USER -> 1
                ROLE_STAFF -> 2
                ROLE_MANAGER -> 3
                ROLE_ADMIN -> 4
                else -> 0
            }
        }
    }
}

data class WorkflowProcess(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val steps: List<WorkflowStepConfig> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: com.google.firebase.Timestamp? = null
)

data class WorkflowStepConfig(
    val stepOrder: Int = 0,
    val stepName: String = "",
    val requiredRole: String = "staff", // Role tối thiểu để xử lý bước này
    val actionType: String = "review", // review, approve, complete
    val canReassign: Boolean = false,
    val timeoutHours: Int = 24
)
