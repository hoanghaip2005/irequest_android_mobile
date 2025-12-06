package com.irequest.ui.data

data class RequestItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val date: String,
    val status: String = "NEW",
    val assignee: String = "Chưa phân công",
    val deadline: String = "03/12/2025"
)

object RequestStore {
    private val requests = mutableListOf(
        RequestItem(
            id = "1",
            title = "Yêu cầu sửa chữa máy lạnh",
            description = "Máy lạnh phòng họp không hoạt động, cần kiểm tra và sửa chữa ngay",
            category = "IT",
            priority = "CAO",
            date = "2025-12-03",
            status = "NEW",
            assignee = "Nguyễn Văn A",
            deadline = "03/12/2025"
        ),
        RequestItem(
            id = "2",
            title = "Yêu cầu về sinh văn phòng",
            description = "Vệ sinh toàn bộ văn phòng",
            category = "Vệ sinh",
            priority = "TRUNG",
            date = "2025-12-02",
            status = "IN_PROGRESS",
            assignee = "Trần Thị B",
            deadline = "05/12/2025"
        ),
        RequestItem(
            id = "3",
            title = "Yêu cầu cấp tài liệu",
            description = "Cấp tài liệu hành chính cho bộ phận",
            category = "Hành chính",
            priority = "THẤP",
            date = "2025-12-01",
            status = "DONE",
            assignee = "Lê Văn C",
            deadline = "01/12/2025"
        ),
        RequestItem(
            id = "4",
            title = "Yêu cầu bảo hành thiết bị",
            description = "Bảo hành thiết bị công nghệ",
            category = "IT",
            priority = "CAO",
            date = "2025-11-30",
            status = "NEW",
            assignee = "Chưa phân công",
            deadline = "10/12/2025"
        )
    )

    fun getAllRequests(): List<RequestItem> = requests.sortedByDescending { it.id }

    fun getRequestById(id: String): RequestItem? = requests.find { it.id == id }

    fun addRequest(request: RequestItem) {
        requests.add(0, request)
    }

    fun updateRequest(id: String, request: RequestItem) {
        val index = requests.indexOfFirst { it.id == id }
        if (index >= 0) {
            requests[index] = request
        }
    }

    fun deleteRequest(id: String) {
        requests.removeAll { it.id == id }
    }
}
