package com.example.irequest.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

// 1. Tạo thêm class Nhân viên (để ngay trong file này cũng được cho gọn)
data class Employee(
    val name: String,
    val role: String, // Chức vụ: Nhân viên, Phó phòng...
    val avatarUrl: String? = null // Để sau này có ảnh thì dùng, giờ chưa cần
)

// 2. Cập nhật Department có thêm danh sách nhân viên
data class Department(
    @DocumentId
    val departmentId: Int = 0,
    val name: String = "",
    val description: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isActive: Boolean = true,
    val assignedUserName: String? = null,

    // 👇 THÊM DANH SÁCH NHÂN VIÊN VÀO ĐÂY
    val employees: List<Employee> = emptyList()
) {
    @get:Exclude
    var isExpanded: Boolean = false
}