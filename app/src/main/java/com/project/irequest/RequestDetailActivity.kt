package com.project.irequest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.irequest.data.models.Request
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RequestDetailActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var chipStatus: Chip
    private lateinit var chipPriority: Chip
    private lateinit var tvRequestId: TextView
    private lateinit var tvCreatedBy: TextView
    private lateinit var tvCreatedDate: TextView
    private lateinit var tvAssignedTo: TextView
    private lateinit var tvUpdatedDate: TextView
    private lateinit var layoutAttachment: MaterialCardView
    private lateinit var tvAttachmentName: TextView
    private lateinit var btnDownloadAttachment: Button
    private lateinit var progressBar: ProgressBar
    
    private val requestRepository = FirebaseRequestRepository()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    private var requestId: String? = null
    private var currentRequest: Request? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)
        
        requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
        
        initViews()
        setupToolbar()
        
        requestId?.let {
            loadRequestDetail(it)
        } ?: run {
            Toast.makeText(this, "Không tìm thấy thông tin yêu cầu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        chipStatus = findViewById(R.id.chipStatus)
        chipPriority = findViewById(R.id.chipPriority)
        tvRequestId = findViewById(R.id.tvRequestId)
        tvCreatedBy = findViewById(R.id.tvCreatedBy)
        tvCreatedDate = findViewById(R.id.tvCreatedDate)
        tvAssignedTo = findViewById(R.id.tvAssignedTo)
        tvUpdatedDate = findViewById(R.id.tvUpdatedDate)
        layoutAttachment = findViewById(R.id.layoutAttachment)
        tvAttachmentName = findViewById(R.id.tvAttachmentName)
        btnDownloadAttachment = findViewById(R.id.btnDownloadAttachment)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Chi tiết yêu cầu"
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun loadRequestDetail(requestId: String) {
        showLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    // Dùng version String của getRequestById
                    requestRepository.getRequestById(requestId)
                }
                
                result.onSuccess { request ->
                    currentRequest = request
                    displayRequestDetail(request)
                }.onFailure { e ->
                    Toast.makeText(
                        this@RequestDetailActivity,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestDetailActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun displayRequestDetail(request: Request) {
        // Basic info
        tvTitle.text = request.title
        tvDescription.text = request.description ?: "Không có mô tả"
        tvRequestId.text = "ID: #${request.requestId}"
        
        // Status
        chipStatus.text = request.statusName ?: "Mới"
        val (statusBgColor, statusStrokeColor) = when (request.statusId) {
            1 -> Pair("#E3F2FD", "#2196F3") // New
            2 -> Pair("#FFF3E0", "#FF9800") // In Progress
            3 -> Pair("#E8F5E9", "#4CAF50") // Completed
            4 -> Pair("#F5F5F5", "#9E9E9E") // Closed
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
            1 -> Pair("#E8F5E9", "#4CAF50") // Low
            2 -> Pair("#E3F2FD", "#2196F3") // Medium
            3 -> Pair("#FFF3E0", "#FF9800") // High
            4 -> Pair("#FFEBEE", "#F44336") // Urgent
            else -> Pair("#F5F5F5", "#9E9E9E")
        }
        chipPriority.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityBgColor)
        )
        chipPriority.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityStrokeColor)
        )
        
        // User info
        tvCreatedBy.text = request.userName ?: "Không rõ"
        request.createdAt?.let {
            tvCreatedDate.text = dateFormat.format(it)
        }
        
        tvAssignedTo.text = request.assignedUserName ?: "Chưa phân công"
        
        request.updatedAt?.let {
            tvUpdatedDate.text = "Cập nhật: ${dateFormat.format(it)}"
        }
        
        // Attachment
        if (request.attachmentUrl != null && request.attachmentFileName != null) {
            layoutAttachment.visibility = View.VISIBLE
            tvAttachmentName.text = request.attachmentFileName
            
            btnDownloadAttachment.setOnClickListener {
                openAttachment(request.attachmentUrl)
            }
        } else {
            layoutAttachment.visibility = View.GONE
        }
    }
    
    private fun openAttachment(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở file", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    companion object {
        const val EXTRA_REQUEST_ID = "extra_request_id"
    }
}
