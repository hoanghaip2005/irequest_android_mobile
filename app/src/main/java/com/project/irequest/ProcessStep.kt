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
    val title: String,
    val description: String,
    val date: String,
    val status: StepStatus
)
