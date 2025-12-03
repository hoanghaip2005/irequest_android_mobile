package com.example.irequest.data.models.request

/**
 * Login request model
 */
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = false
)

/**
 * Register request model
 */
data class RegisterRequest(
    val userName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phoneNumber: String? = null,
    val departmentId: Int? = null
)

/**
 * Create request model
 */
data class CreateRequestModel(
    val title: String,
    val description: String? = null,
    val priorityId: Int? = null,
    val workflowId: Int? = null,
    val departmentId: Int? = null,
    val attachmentUrl: String? = null,
    val attachmentFileName: String? = null,
    val attachmentFileType: String? = null,
    val attachmentFileSize: Long? = null
)

/**
 * Update request model
 */
data class UpdateRequestModel(
    val requestId: Int,
    val title: String? = null,
    val description: String? = null,
    val priorityId: Int? = null,
    val statusId: Int? = null,
    val assignedUserId: String? = null
)

/**
 * Add comment request model
 */
data class AddCommentRequest(
    val requestId: Int,
    val content: String
)

/**
 * Update starred request model
 */
data class UpdateStarredRequest(
    val requestId: Int,
    val isStarred: Boolean
)

/**
 * Reject request model
 */
data class RejectRequestModel(
    val requestId: Int,
    val reason: String,
    val notes: String? = null
)

/**
 * Approve request model
 */
data class ApproveRequestModel(
    val requestId: Int,
    val notes: String? = null,
    val nextStepId: Int? = null
)

/**
 * Rate request model
 */
data class RateRequestModel(
    val requestId: Int,
    val rating: Int, // 1-5
    val feedback: String? = null
)

/**
 * Change password request model
 */
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

/**
 * Update profile request model
 */
data class UpdateProfileRequest(
    val userName: String? = null,
    val phoneNumber: String? = null,
    val homeAddress: String? = null,
    val birthDate: String? = null, // ISO format
    val avatar: String? = null
)

/**
 * Reset password request model
 */
data class ResetPasswordRequest(
    val email: String
)

/**
 * Confirm reset password model
 */
data class ConfirmResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)
