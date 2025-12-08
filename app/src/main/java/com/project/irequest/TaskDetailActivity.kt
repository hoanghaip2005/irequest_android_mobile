package com.project.irequest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.irequest.data.models.Request
import com.example.irequest.data.models.request.UpdateRequestModel
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_TASK_ID = "requestId"
    }

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCreatedBy: TextView
    private lateinit var tvCreatedDate: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvAssignee: TextView
    private lateinit var tvAttachment: TextView
    private lateinit var chipStatus: Chip
    private lateinit var chipPriority: Chip
    private lateinit var spinnerStatus: Spinner
    private lateinit var etComment: TextInputEditText
    private lateinit var btnAddComment: Button
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var lvComments: ListView
    
    private val repository = FirebaseRequestRepository()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private var requestId: String? = null
    private var currentRequest: Request? = null
    private val comments = mutableListOf<String>()
    private lateinit var commentAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Chi tiết công việc"
        
        requestId = intent.getStringExtra("requestId")
        
        initViews()
        setupStatusSpinner()
        setupComments()
        
        requestId?.let {
            loadTaskDetail(it)
        } ?: run {
            Toast.makeText(this, "Không tìm thấy công việc", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun initViews() {
        tvTitle = findViewById(R.id.tvTaskDetailTitle)
        tvDescription = findViewById(R.id.tvTaskDetailDescription)
        tvCreatedBy = findViewById(R.id.tvTaskCreatedBy)
        tvCreatedDate = findViewById(R.id.tvTaskCreatedDate)
        tvDueDate = findViewById(R.id.tvTaskDueDate)
        tvAssignee = findViewById(R.id.tvTaskAssignee)
        tvAttachment = findViewById(R.id.tvTaskAttachment)
        chipStatus = findViewById(R.id.chipTaskStatus)
        chipPriority = findViewById(R.id.chipTaskPriority)
        spinnerStatus = findViewById(R.id.spinnerUpdateStatus)
        etComment = findViewById(R.id.etTaskComment)
        btnAddComment = findViewById(R.id.btnAddComment)
        btnSave = findViewById(R.id.btnSaveTask)
        progressBar = findViewById(R.id.progressBarTask)
        lvComments = findViewById(R.id.lvTaskComments)
        
        btnAddComment.setOnClickListener { addComment() }
        btnSave.setOnClickListener { saveTaskUpdate() }
    }
    
    private fun setupStatusSpinner() {
        val statusOptions = arrayOf("Mới", "Đang xử lý", "Hoàn thành", "Đóng")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }
    
    private fun setupComments() {
        commentAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, comments)
        lvComments.adapter = commentAdapter
    }
    
    private fun loadTaskDetail(id: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    repository.getRequestById(id.toInt())
                }
                
                result.onSuccess { request ->
                    currentRequest = request
                    displayTaskDetails(request)
                }.onFailure { exception ->
                    Toast.makeText(
                        this@TaskDetailActivity,
                        "Lỗi: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@TaskDetailActivity,
                    "Lỗi khi tải dữ liệu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun displayTaskDetails(request: Request) {
        tvTitle.text = request.title
        tvDescription.text = request.description ?: "Không có mô tả"
        tvCreatedBy.text = "Người tạo: ${request.userName ?: "Không rõ"}"
        
        request.createdAt?.let {
            tvCreatedDate.text = "Ngày tạo: ${dateFormat.format(it)}"
        } ?: run {
            tvCreatedDate.text = "Ngày tạo: N/A"
        }
        
        // Hide due date field if not available
        tvDueDate.visibility = View.GONE
        
        tvAssignee.text = "Người xử lý: ${FirebaseAuth.getInstance().currentUser?.displayName ?: "Bạn"}"
        
        request.attachmentUrl?.let {
            tvAttachment.text = "File đính kèm: ${request.attachmentFileName ?: "file"}"
            tvAttachment.visibility = View.VISIBLE
        } ?: run {
            tvAttachment.visibility = View.GONE
        }
        
        // Status
        chipStatus.text = request.statusName ?: "Mới"
        val (statusBgColor, statusStrokeColor) = when (request.statusId) {
            1 -> Pair("#E3F2FD", "#2196F3")
            2 -> Pair("#FFF3E0", "#FF9800")
            3 -> Pair("#E8F5E9", "#4CAF50")
            4 -> Pair("#F5F5F5", "#9E9E9E")
            else -> Pair("#F5F5F5", "#000000")
        }
        chipStatus.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(statusBgColor)
        )
        chipStatus.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(statusStrokeColor)
        )
        
        // Priority
        chipPriority.text = request.priorityName ?: "Thường"
        val (priorityBgColor, priorityStrokeColor) = when (request.priorityId) {
            1 -> Pair("#E8F5E9", "#4CAF50")
            2 -> Pair("#E3F2FD", "#2196F3")
            3 -> Pair("#FFF3E0", "#FF9800")
            4 -> Pair("#FFEBEE", "#F44336")
            else -> Pair("#F5F5F5", "#9E9E9E")
        }
        chipPriority.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityBgColor)
        )
        chipPriority.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityStrokeColor)
        )
        
        // Set spinner to current status
        spinnerStatus.setSelection(request.statusId?.minus(1) ?: 0)
        
        // Comments section is available for adding new comments
        comments.clear()
        commentAdapter.notifyDataSetChanged()
    }
    
    private fun addComment() {
        val comment = etComment.text.toString().trim()
        if (comment.isEmpty()) {
            etComment.error = "Vui lòng nhập nhận xét"
            return
        }
        
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val formattedComment = "[$timestamp] $userName: $comment"
        
        comments.add(0, formattedComment)
        commentAdapter.notifyDataSetChanged()
        etComment.text?.clear()
        
        Toast.makeText(this, "Đã thêm nhận xét", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveTaskUpdate() {
        val selectedStatusPosition = spinnerStatus.selectedItemPosition
        val newStatusId = selectedStatusPosition + 1
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                requestId?.let { id ->
                    // Update status with UpdateRequestModel
                    val updateModel = UpdateRequestModel(
                        requestId = id.toInt(),
                        statusId = newStatusId
                    )
                    
                    withContext(Dispatchers.IO) {
                        repository.updateRequest(updateModel)
                    }
                    
                    Toast.makeText(
                        this@TaskDetailActivity,
                        "Cập nhật thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@TaskDetailActivity,
                    "Lỗi khi cập nhật: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !isLoading
        btnAddComment.isEnabled = !isLoading
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
