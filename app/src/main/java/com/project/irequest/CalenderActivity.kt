package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.example.irequest.data.repository.FirebaseRequestRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalenderActivity : BaseActivity() {

    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var requestsRecyclerView: RecyclerView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvNoRequests: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var requestCardAdapter: RequestCardAdapter
    private lateinit var requestRepository: FirebaseRequestRepository
    
    private val calendar = Calendar.getInstance()
    private var allRequests = listOf<Request>()
    
    private val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM, yyyy", Locale("vi"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calender)

        initViews()
        setupRecyclerViews()
        setupClickListeners()
        
        // Initialize repository
        requestRepository = FirebaseRequestRepository()
        
        // Load requests and calendar
        loadRequests()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        setActiveTab(0)
    }

    private fun initViews() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView)
        requestsRecyclerView = findViewById(R.id.requestsRecyclerView)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvNoRequests = findViewById(R.id.tvNoRequests)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
    }

    private fun setupRecyclerViews() {
        // Calendar grid (7 columns)
        calendarAdapter = CalendarAdapter(emptyList()) { day ->
            onDaySelected(day)
        }
        calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(this@CalenderActivity, 7)
            adapter = calendarAdapter
        }
        
        // Request cards list
        requestCardAdapter = RequestCardAdapter(emptyList()) { request ->
            onRequestClick(request)
        }
        requestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CalenderActivity)
            adapter = requestCardAdapter
        }
    }

    private fun setupClickListeners() {
        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        // Long click on month/year to reload data
        tvMonthYear.setOnLongClickListener {
            Toast.makeText(this, "Đang tải lại dữ liệu...", Toast.LENGTH_SHORT).show()
            loadRequests()
            true
        }
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@CalenderActivity,
                    "Đang tải dữ liệu...",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Try to load all requests first (for testing/admin view)
                val allRequestsResult = requestRepository.getAllRequests(pageSize = 200)
                
                // If that fails or returns empty, try user-specific requests
                var requests = allRequestsResult.getOrNull() ?: emptyList()
                
                if (requests.isEmpty()) {
                    // Load both my requests and assigned tasks
                    val myRequestsResult = requestRepository.getMyRequests(pageSize = 100)
                    val myTasksResult = requestRepository.getMyTasks(pageSize = 100)
                    
                    val myRequests = myRequestsResult.getOrNull() ?: emptyList()
                    val myTasks = myTasksResult.getOrNull() ?: emptyList()
                    
                    requests = myRequests + myTasks
                    
                    android.util.Log.d("CalenderActivity", "My Requests: ${myRequests.size}")
                    android.util.Log.d("CalenderActivity", "My Tasks: ${myTasks.size}")
                } else {
                    android.util.Log.d("CalenderActivity", "All Requests: ${requests.size}")
                }
                
                // Combine and remove duplicates
                allRequests = requests
                    .distinctBy { it.id }
                    .filter { it.createdAt != null }
                
                android.util.Log.d("CalenderActivity", "Total Requests with date: ${allRequests.size}")
                
                // Log first few requests for debugging
                allRequests.take(3).forEach { request ->
                    android.util.Log.d("CalenderActivity", "Request #${request.requestId}: ${request.title} - Created: ${request.createdAt}")
                }
                
                // Show result message
                Toast.makeText(
                    this@CalenderActivity,
                    "Đã tải ${allRequests.size} requests",
                    Toast.LENGTH_SHORT
                ).show()
                
                updateCalendar()
                
            } catch (e: Exception) {
                android.util.Log.e("CalenderActivity", "Error loading requests", e)
                Toast.makeText(
                    this@CalenderActivity,
                    "Lỗi tải dữ liệu: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun updateCalendar() {
        // Update month/year header
        tvMonthYear.text = monthYearFormat.format(calendar.time)
        
        // Generate calendar days
        val days = generateCalendarDays()
        
        // Debug: count days with requests
        val daysWithRequests = days.count { it.hasRequests }
        android.util.Log.d("CalenderActivity", "Calendar updated: $daysWithRequests days have requests")
        
        calendarAdapter.updateDays(days)
    }

    private fun generateCalendarDays(): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        
        // Get first day of month
        val tempCal = calendar.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Adjust for Monday start (DAY_OF_WEEK: 1=Sunday, 2=Monday, ...)
        val startOffset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        
        // Add previous month days
        tempCal.add(Calendar.DAY_OF_MONTH, -startOffset)
        for (i in 0 until startOffset) {
            val dayDate = tempCal.time
            days.add(
                CalendarDay(
                    date = dayDate,
                    dayOfMonth = tempCal.get(Calendar.DAY_OF_MONTH),
                    hasRequests = false,
                    isCurrentMonth = false,
                    isToday = false
                )
            )
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Add current month days
        tempCal.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        tempCal.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        
        val today = Calendar.getInstance()
        
        for (day in 1..daysInMonth) {
            val dayDate = tempCal.time
            val hasRequests = hasRequestsOnDate(dayDate)
            val isToday = isSameDay(tempCal, today)
            
            days.add(
                CalendarDay(
                    date = dayDate,
                    dayOfMonth = day,
                    hasRequests = hasRequests,
                    isCurrentMonth = true,
                    isToday = isToday
                )
            )
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Add next month days to complete the grid (42 cells = 6 rows)
        val remainingCells = 42 - days.size
        for (i in 0 until remainingCells) {
            val dayDate = tempCal.time
            days.add(
                CalendarDay(
                    date = dayDate,
                    dayOfMonth = tempCal.get(Calendar.DAY_OF_MONTH),
                    hasRequests = false,
                    isCurrentMonth = false,
                    isToday = false
                )
            )
            tempCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return days
    }

    private fun hasRequestsOnDate(date: Date): Boolean {
        val cal = Calendar.getInstance()
        cal.time = date
        
        return allRequests.any { request ->
            request.createdAt?.let { createdAt ->
                val requestCal = Calendar.getInstance()
                requestCal.time = createdAt
                isSameDay(cal, requestCal)
            } ?: false
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun onDaySelected(day: CalendarDay) {
        if (day.date == null || !day.isCurrentMonth) return
        
        // Filter requests for selected date
        val requestsOnDate = allRequests.filter { request ->
            request.createdAt?.let { createdAt ->
                val cal = Calendar.getInstance()
                cal.time = day.date
                
                val requestCal = Calendar.getInstance()
                requestCal.time = createdAt
                
                isSameDay(cal, requestCal)
            } ?: false
        }
        
        // Update UI
        tvSelectedDate.text = "Công việc ngày ${dateFormat.format(day.date)}"
        tvSelectedDate.visibility = View.VISIBLE
        
        if (requestsOnDate.isNotEmpty()) {
            requestCardAdapter.updateRequests(requestsOnDate)
            requestsRecyclerView.visibility = View.VISIBLE
            tvNoRequests.visibility = View.GONE
        } else {
            requestsRecyclerView.visibility = View.GONE
            tvNoRequests.visibility = View.VISIBLE
        }
    }

    private fun onRequestClick(request: Request) {
        // Show dialog with request details
        showRequestDetailDialog(request)
    }
    
    private fun showRequestDetailDialog(request: Request) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_request_detail, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // Set background transparent for rounded corners
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Bind data to dialog views
        dialogView.findViewById<TextView>(R.id.tvDialogRequestId).text = "#${request.requestId}"
        dialogView.findViewById<TextView>(R.id.tvDialogTitle).text = request.title
        dialogView.findViewById<TextView>(R.id.tvDialogDescription).text = 
            request.description ?: "Không có mô tả"
        
        // Status
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvDialogStatus)
        tvStatus.text = request.statusName ?: "Chưa xử lý"
        tvStatus.setBackgroundColor(getStatusColor(request.statusId))
        
        // Priority
        val tvPriority = dialogView.findViewById<TextView>(R.id.tvDialogPriority)
        tvPriority.text = request.priorityName ?: "Thường"
        tvPriority.setTextColor(getPriorityColor(request.priorityId))
        
        // Workflow
        val layoutWorkflow = dialogView.findViewById<View>(R.id.layoutWorkflow)
        val tvWorkflow = dialogView.findViewById<TextView>(R.id.tvDialogWorkflow)
        if (request.workflowName != null) {
            tvWorkflow.text = request.workflowName
            layoutWorkflow.visibility = View.VISIBLE
        } else {
            layoutWorkflow.visibility = View.GONE
        }
        
        // Department
        val layoutDepartment = dialogView.findViewById<View>(R.id.layoutDepartment)
        val tvDepartment = dialogView.findViewById<TextView>(R.id.tvDialogDepartment)
        if (request.departmentName != null) {
            tvDepartment.text = request.departmentName
            layoutDepartment.visibility = View.VISIBLE
        } else {
            layoutDepartment.visibility = View.GONE
        }
        
        // Creator
        dialogView.findViewById<TextView>(R.id.tvDialogCreator).text = 
            request.userName ?: request.userEmail ?: "Không rõ"
        
        // Assignee
        val layoutAssignee = dialogView.findViewById<View>(R.id.layoutAssignee)
        val tvAssignee = dialogView.findViewById<TextView>(R.id.tvDialogAssignee)
        if (request.assignedUserName != null) {
            tvAssignee.text = request.assignedUserName
            layoutAssignee.visibility = View.VISIBLE
        } else {
            layoutAssignee.visibility = View.GONE
        }
        
        // Created Date
        dialogView.findViewById<TextView>(R.id.tvDialogCreatedDate).text = 
            request.createdAt?.let { dateTimeFormat.format(it) } ?: "Không rõ"
        
        // Updated Date
        val layoutUpdatedDate = dialogView.findViewById<View>(R.id.layoutUpdatedDate)
        val tvUpdatedDate = dialogView.findViewById<TextView>(R.id.tvDialogUpdatedDate)
        if (request.updatedAt != null) {
            tvUpdatedDate.text = dateTimeFormat.format(request.updatedAt)
            layoutUpdatedDate.visibility = View.VISIBLE
        } else {
            layoutUpdatedDate.visibility = View.GONE
        }
        
        // Attachment
        val layoutAttachment = dialogView.findViewById<View>(R.id.layoutAttachment)
        val tvAttachment = dialogView.findViewById<TextView>(R.id.tvDialogAttachment)
        if (request.attachmentFileName != null) {
            tvAttachment.text = request.attachmentFileName
            layoutAttachment.visibility = View.VISIBLE
            layoutAttachment.setOnClickListener {
                Toast.makeText(this, "Tải file: ${request.attachmentFileName}", Toast.LENGTH_SHORT).show()
                // TODO: Download or open attachment
            }
        } else {
            layoutAttachment.visibility = View.GONE
        }
        
        // Buttons
        dialogView.findViewById<Button>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btnViewDetail).setOnClickListener {
            dialog.dismiss()
            Toast.makeText(this, "Chuyển đến chi tiết request #${request.requestId}", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to RequestDetailActivity
            // val intent = Intent(this, RequestDetailActivity::class.java)
            // intent.putExtra("REQUEST_ID", request.requestId)
            // startActivity(intent)
        }
        
        dialog.show()
    }
    
    private fun getStatusColor(statusId: Int?): Int {
        return when (statusId) {
            1 -> android.graphics.Color.parseColor("#FF9800") // Pending - Orange
            2 -> android.graphics.Color.parseColor("#2196F3") // In Progress - Blue
            3 -> android.graphics.Color.parseColor("#4CAF50") // Completed - Green
            4 -> android.graphics.Color.parseColor("#F44336") // Rejected - Red
            else -> android.graphics.Color.parseColor("#9E9E9E") // Unknown - Gray
        }
    }
    
    private fun getPriorityColor(priorityId: Int?): Int {
        return when (priorityId) {
            1 -> android.graphics.Color.parseColor("#4CAF50") // Low - Green
            2 -> android.graphics.Color.parseColor("#FF9800") // Medium - Orange
            3 -> android.graphics.Color.parseColor("#F44336") // High - Red
            4 -> android.graphics.Color.parseColor("#9C27B0") // Urgent - Purple
            else -> android.graphics.Color.parseColor("#666666") // Unknown - Gray
        }
    }

    override fun onNavigationHomeClicked() {
        finish()
    }
    
    override fun onNavigationWorkClicked() {
        Toast.makeText(this, "Work", Toast.LENGTH_SHORT).show()
    }
    
    override fun onNavigationChatClicked() {
        Toast.makeText(this, "Chat", Toast.LENGTH_SHORT).show()
    }
    
    override fun onNavigationAccountClicked() {
        Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show()
    }
}
