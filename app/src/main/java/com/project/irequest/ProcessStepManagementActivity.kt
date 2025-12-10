package com.project.irequest

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.FirebaseWorkflowManagementRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProcessStepManagementActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProcessStepAdapter
    private lateinit var tvProcessName: TextView
    private lateinit var tvProcessDescription: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    
    private val workflowRepository = FirebaseWorkflowManagementRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_step_management)

        setupViews()
        
        // Get process info from intent (if launched from ProcessAdapter)
        val processId = intent.getIntExtra("PROCESS_ID", -1)
        val processName = intent.getStringExtra("PROCESS_NAME") ?: "Quy trình mẫu"
        val processStatus = intent.getStringExtra("PROCESS_STATUS") ?: ""
        
        if (processId != -1) {
            loadWorkflowStepsFromFirebase(processId, processName, processStatus)
        } else {
            // Show sample data if no process selected
            loadSampleProcessSteps(processName)
        }
        
        setupBottomNavigation()
        setActiveTab(0)
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.rvProcessSteps)
        tvProcessName = findViewById(R.id.tvProcessName)
        tvProcessDescription = findViewById(R.id.tvProcessDescription)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadWorkflowStepsFromFirebase(processId: Int, processName: String, processStatus: String) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Get workflow steps from Firestore
                val result = workflowRepository.getWorkflowSteps(processId)
                
                result.onSuccess { workflowSteps ->
                    progressBar.visibility = View.GONE
                    
                    if (workflowSteps.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Chưa có bước quy trình nào"
                        Toast.makeText(
                            this@ProcessStepManagementActivity,
                            "Không tìm thấy bước cho quy trình này",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadSampleProcessSteps(processName) // Fallback to sample
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        
                        tvProcessName.text = processName
                        tvProcessDescription.text = "Trạng thái: $processStatus • ${workflowSteps.size} bước"
                        
                        // Convert WorkflowStep to ProcessStep
                        val processSteps = workflowSteps.map { step ->
                            ProcessStep(
                                stepId = step.stepId.toString(),
                                workflowId = step.workflowId.toString(),
                                title = step.stepName,
                                description = buildStepDescription(step),
                                date = "", // WorkflowStep không có date
                                status = getStepStatus(step),
                                assignee = step.assignedUserName,
                                department = step.departmentName,
                                timeLimit = step.timeLimitHours?.let { "$it giờ" }
                            )
                        }
                        
                        adapter = ProcessStepAdapter(processSteps) { step ->
                            showEditStepDialog(step)
                        }
                        recyclerView.adapter = adapter
                        
                        Toast.makeText(
                            this@ProcessStepManagementActivity,
                            "Đã tải ${workflowSteps.size} bước quy trình",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                
                result.onFailure { error ->
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Lỗi: ${error.message}"
                    Toast.makeText(
                        this@ProcessStepManagementActivity,
                        "Lỗi tải dữ liệu: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Fallback to sample data
                    loadSampleProcessSteps(processName)
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Lỗi: ${e.message}"
                Toast.makeText(
                    this@ProcessStepManagementActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun buildStepDescription(step: com.example.irequest.data.models.WorkflowStep): String {
        val parts = mutableListOf<String>()
        
        step.assignedUserName?.let { parts.add("Người xử lý: $it") }
        step.departmentName?.let { parts.add("Phòng ban: $it") }
        step.requiredRoleName?.let { parts.add("Quyền yêu cầu: $it") }
        step.timeLimitHours?.let { parts.add("Thời hạn: $it giờ") }
        
        if (step.requiresApproval) parts.add("Yêu cầu phê duyệt")
        if (step.canDelegate) parts.add("Có thể ủy quyền")
        
        return if (parts.isEmpty()) {
            "Bước trong quy trình"
        } else {
            parts.joinToString(" • ")
        }
    }
    
    private fun getStepStatus(step: com.example.irequest.data.models.WorkflowStep): StepStatus {
        // Map statusId to StepStatus
        return when (step.statusId) {
            3 -> StepStatus.COMPLETED  // Completed
            2 -> StepStatus.CURRENT    // In Progress
            1 -> StepStatus.PENDING    // Pending
            else -> StepStatus.UPCOMING
        }
    }

    private fun loadSampleProcessSteps(processName: String) {
        tvProcessName.text = processName
        tvProcessDescription.text = "Quy trình mẫu với 6 bước xử lý"
        
        val sampleSteps = listOf(
            ProcessStep(
                title = "Khởi tạo yêu cầu",
                description = "Tạo yêu cầu mới và gửi đi • Phòng ban: Kế hoạch",
                date = "01/01/2024",
                status = StepStatus.COMPLETED
            ),
            ProcessStep(
                title = "Phê duyệt cấp 1",
                description = "Trưởng phòng xem xét và phê duyệt • Thời hạn: 24 giờ",
                date = "02/01/2024",
                status = StepStatus.COMPLETED
            ),
            ProcessStep(
                title = "Thực hiện",
                description = "Bộ phận liên quan thực hiện yêu cầu • Yêu cầu phê duyệt",
                date = "03/01/2024",
                status = StepStatus.CURRENT
            ),
            ProcessStep(
                title = "Kiểm tra",
                description = "Phòng QC kiểm tra kết quả thực hiện • Có thể ủy quyền",
                date = "04/01/2024",
                status = StepStatus.PENDING
            ),
            ProcessStep(
                title = "Phê duyệt cấp 2",
                description = "Ban giám đốc phê duyệt cuối cùng • Thời hạn: 48 giờ",
                date = "05/01/2024",
                status = StepStatus.UPCOMING
            ),
            ProcessStep(
                title = "Hoàn tất",
                description = "Đóng yêu cầu và lưu trữ hồ sơ",
                date = "06/01/2024",
                status = StepStatus.UPCOMING
            )
        )
        
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        adapter = ProcessStepAdapter(sampleSteps) { step ->
            showEditStepDialog(step)
        }
        recyclerView.adapter = adapter
    }

    override fun onNavigationHomeClicked() {
        finish()
    }
    
    private fun showEditStepDialog(step: ProcessStep) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_step, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val etStepTitle = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStepTitle)
        val etStepDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etStepDescription)
        val spinnerAssignee = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerAssignee)
        val spinnerStatus = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerStatus)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSave)
        
        // Populate data
        etStepTitle.setText(step.title)
        etStepDescription.setText(step.description)
        
        // Load users from Firebase for assignee spinner
        loadUsersForSpinner(spinnerAssignee, step.assignee)
        
        // Setup status spinner
        val statusArray = arrayOf("Chờ xử lý", "Đang xử lý", "Hoàn thành", "Sắp tới")
        val statusAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, statusArray)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
        
        // Set current status
        val statusPosition = when (step.status) {
            StepStatus.PENDING -> 0
            StepStatus.CURRENT -> 1
            StepStatus.COMPLETED -> 2
            StepStatus.UPCOMING -> 3
        }
        spinnerStatus.setSelection(statusPosition)
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        btnSave.setOnClickListener {
            val newTitle = etStepTitle.text.toString().trim()
            val newDescription = etStepDescription.text.toString().trim()
            val selectedUser = spinnerAssignee.selectedItem as? UserItem
            val newAssignee = selectedUser?.userName ?: ""
            val newAssigneeId = selectedUser?.userId ?: ""
            val newStatus = when (spinnerStatus.selectedItemPosition) {
                0 -> "PENDING"
                1 -> "CURRENT"
                2 -> "COMPLETED"
                3 -> "UPCOMING"
                else -> "PENDING"
            }
            
            if (newTitle.isEmpty()) {
                etStepTitle.error = "Vui lòng nhập tiêu đề"
                return@setOnClickListener
            }
            
            // Save to Firebase
            saveStepToFirebase(step.stepId, newTitle, newDescription, newAssigneeId, newAssignee, newStatus, dialog)
        }
        
        dialog.show()
    }
    
    private fun loadUsersForSpinner(spinner: android.widget.Spinner, currentAssignee: String?) {
        lifecycleScope.launch {
            try {
                val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val snapshot = db.collection("users").get().await()
                
                val users = mutableListOf<UserItem>()
                users.add(UserItem("", "-- Chọn người phụ trách --", "", null, null)) // Default option
                
                snapshot.documents.forEach { doc ->
                    val id = doc.id
                    val name = doc.getString("userName") ?: doc.getString("email") ?: "User"
                    val email = doc.getString("email") ?: ""
                    val department = doc.getString("departmentName")
                    val avatar = doc.getString("avatar")
                    users.add(UserItem(id, name, email, department, avatar))
                }
                
                val adapter = android.widget.ArrayAdapter(
                    this@ProcessStepManagementActivity,
                    android.R.layout.simple_spinner_item,
                    users
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                
                // Set current assignee if exists
                if (currentAssignee != null) {
                    val position = users.indexOfFirst { it.userName == currentAssignee }
                    if (position >= 0) {
                        spinner.setSelection(position)
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ProcessStepManagementActivity,
                    "Lỗi tải danh sách người dùng: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun saveStepToFirebase(stepId: String, title: String, description: String, assigneeId: String, assigneeName: String, status: String, dialog: androidx.appcompat.app.AlertDialog) {
        if (stepId.isEmpty()) {
            Toast.makeText(this, "Không thể cập nhật: thiếu stepId", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }
        
        lifecycleScope.launch {
            try {
                val result = workflowRepository.updateWorkflowStep(stepId, title, description, assigneeId, assigneeName, status)
                
                result.onSuccess {
                    Toast.makeText(
                        this@ProcessStepManagementActivity,
                        "Đã cập nhật bước quy trình",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    
                    // Reload data
                    val processId = intent.getIntExtra("PROCESS_ID", -1)
                    val processName = intent.getStringExtra("PROCESS_NAME") ?: "Quy trình"
                    val processStatus = intent.getStringExtra("PROCESS_STATUS") ?: ""
                    if (processId != -1) {
                        loadWorkflowStepsFromFirebase(processId, processName, processStatus)
                    }
                }
                
                result.onFailure { error ->
                    Toast.makeText(
                        this@ProcessStepManagementActivity,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProcessStepManagementActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
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
