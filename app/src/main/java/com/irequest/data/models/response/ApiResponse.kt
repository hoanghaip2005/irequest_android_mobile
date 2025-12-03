package com.example.irequest.data.models.response

/**
 * Generic API Response wrapper
 */
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null,
    val error: String? = null,
    val statusCode: Int = 200
)

/**
 * Paginated response
 */
data class PaginatedResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: List<T> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

/**
 * Authentication response
 */
data class AuthResponse(
    val success: Boolean = false,
    val message: String = "",
    val token: String? = null,
    val refreshToken: String? = null,
    val user: com.example.irequest.data.models.User? = null,
    val expiresIn: Long = 0 // seconds
)

/**
 * Upload file response
 */
data class UploadResponse(
    val success: Boolean = false,
    val message: String = "",
    val fileUrl: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val fileType: String = ""
)
