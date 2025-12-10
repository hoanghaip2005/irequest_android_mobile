package com.project.irequest

// Định nghĩa trạng thái (Enum) để sửa lỗi "Unresolved reference 'StepStatus'"
enum class StepStatus {
    COMPLETED,
    CURRENT,
    PENDING,
    UPCOMING
}

// Định nghĩa dữ liệu bước để sửa lỗi "Unresolved reference 'ProcessStep'"
data class ProcessStep(
    val stepId: String = "",
    val workflowId: String = "",
    val title: String,
    val description: String,
    val date: String,
    val status: StepStatus,
    val assignee: String? = null,  // Người phụ trách
    val department: String? = null,  // Phòng ban
    val timeLimit: String? = null   // Thời hạn
)
