package com.irequest.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager - Quản lý phiên làm việc của user
 */
class SessionManager(private val context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("irequest_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    /**
     * Lưu thông tin user
     */
    fun saveUser(userId: String, userName: String, email: String, role: String) {
        preferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    /**
     * Lấy ID user
     */
    fun getUserId(): String = preferences.getString(KEY_USER_ID, "") ?: ""

    /**
     * Lấy tên user
     */
    fun getUserName(): String = preferences.getString(KEY_USER_NAME, "User") ?: "User"

    /**
     * Lấy email user
     */
    fun getUserEmail(): String = preferences.getString(KEY_USER_EMAIL, "") ?: ""

    /**
     * Lấy vai trò user
     * Mặc định: USER (Requester/Normal user)
     */
    fun getUserRole(): String = preferences.getString(KEY_USER_ROLE, RoleName.USER) ?: RoleName.USER

    /**
     * Kiểm tra user đã đăng nhập
     */
    fun isLoggedIn(): Boolean = preferences.getBoolean(KEY_IS_LOGGED_IN, false)

    /**
     * Xóa phiên (logout)
     */
    fun logout() {
        preferences.edit().apply {
            clear()
            apply()
        }
    }

    /**
     * Cập nhật vai trò user
     */
    fun updateRole(role: String) {
        preferences.edit().putString(KEY_USER_ROLE, role).apply()
    }

    /**
     * Kiểm tra user có vai trò cụ thể
     */
    fun hasRole(role: String): Boolean = getUserRole() == role

    /**
     * Kiểm tra user có phải Admin
     */
    fun isAdmin(): Boolean = hasRole(RoleName.ADMIN)

    /**
     * Kiểm tra user có phải Agent
     */
    fun isAgent(): Boolean = hasRole(RoleName.AGENT)

    /**
     * Kiểm tra user có phải Approver
     */
    fun isApprover(): Boolean = hasRole(RoleName.APPROVER)

    /**
     * Kiểm tra user có phải User/Requester
     */
    fun isUser(): Boolean = hasRole(RoleName.USER)
}
