package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.example.irequest.data.models.UserRole
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.example.irequest.data.repository.FirebaseWorkflowRepository
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class WorkActivity : BaseActivity() {
    
    private lateinit var rvTasks: RecyclerView
    private lateinit var tvTaskCount: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSetupAdmin: Button
    
    private val requestRepository = FirebaseRequestRepository()
    private val workflowRepository = FirebaseWorkflowRepository()
    private val tasks = mutableListOf<Request>()
    private lateinit var taskAdapter: WorkTaskAdapter
    private var currentUserRole: UserRole? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        
        initViews()
        setupRecyclerView()
        setupBottomNavigation()
        setActiveTab(1)
        
        // Khởi tạo workflow và role
        setupWorkflow()
        loadUserRole() // Sẽ tự động gọi loadMyTasks() sau khi có role
    }
    
    private fun initViews() {
        rvTasks = findViewById(R.id.rvTasks)
        tvTaskCount = findViewById(R.id.tvTaskCount)
        tvUserRole = findViewById(R.id.tvUserRole)
        btnSetupAdmin = findViewById(R.id.btnSetupAdmin)
        
        // Create progressBar if not exists in layout
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
        
        // Setup button
        btnSetupAdmin.setOnClickListener {
            setupAsAdmin()
        }
    }
    
    private fun setupWorkflow() {
        lifecycleScope.launch {
            try {
                // Tạo workflow mặc định nếu chưa có
                workflowRepository.createDefaultWorkflow()
            } catch (e: Exception) {
                // Workflow đã tồn tại, bỏ qua
            }
        }
    }
    
    private fun setupAsAdmin() {
        AlertDialog.Builder(this)
            .setTitle("Thiết lập Admin")
            .setMessage("Bạn có muốn thiết lập tài khoản hiện tại thành Admin với toàn quyền?")
            .setPositiveButton("Đồng ý") { _, _ ->
                lifecycleScope.launch {
                    try {
                        showLoading(true)
                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        
                        val result = workflowRepository.setAdminRole(userId)
                        result.onSuccess {
                            Toast.makeText(this@WorkActivity, "✅ Đã thiết lập Admin thành công", Toast.LENGTH_SHORT).show()
                            loadUserRole()
                            btnSetupAdmin.visibility = View.GONE
                        }.onFailure { e ->
                            Toast.makeText(this@WorkActivity, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        showLoading(false)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun loadUserRole() {
        lifecycleScope.launch {
            try {
                val result = workflowRepository.getCurrentUserRole()
                result.onSuccess { role ->
                    currentUserRole = role
                    updateRoleDisplay(role)
                    // Load tasks AFTER role is loaded
                    loadMyTasks()
                }.onFailure { e ->
                    Toast.makeText(this@WorkActivity, "Không thể tải role: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Vẫn load tasks với role mặc định (user)
                    loadMyTasks()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WorkActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                // Vẫn load tasks
                loadMyTasks()
            }
        }
    }
    
    private fun updateRoleDisplay(role: UserRole) {
        val roleText = when(role.role) {
            UserRole.ROLE_ADMIN -> "👑 Admin - Toàn quyền"
            UserRole.ROLE_MANAGER -> "🔷 Quản lý"
            UserRole.ROLE_STAFF -> "👔 Nhân viên"
            else -> "👤 Người dùng"
        }
        tvUserRole.text = roleText
        
        // Ẩn nút setup admin nếu đã là admin
        if (role.role == UserRole.ROLE_ADMIN) {
            btnSetupAdmin.visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        taskAdapter = WorkTaskAdapter(
            tasks = tasks,
            onItemClick = { request -> openTaskDetail(request) },
            onApproveClick = { request -> approveRequest(request) },
            onRejectClick = { request -> rejectRequest(request) },
            onCompleteClick = { request -> completeRequest(request) },
            canProcess = { request -> checkCanProcess(request) }
        )
        
        rvTasks.apply {
            layoutManager = LinearLayoutManager(this@WorkActivity)
            adapter = taskAdapter
        }
    }
    
    private fun checkCanProcess(request: Request): Boolean {
        val role = currentUserRole ?: return false
        
        // Admin có thể xử lý tất cả
        if (role.role == UserRole.ROLE_ADMIN) return true
        
        // Staff và Manager có thể xử lý request được gán cho mình
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        return request.assignedUserId == userId && 
               (role.role == UserRole.ROLE_STAFF || role.role == UserRole.ROLE_MANAGER)
    }
    
    private fun approveRequest(request: Request) {
        AlertDialog.Builder(this)
            .setTitle("Phê duyệt yêu cầu")
            .setMessage("Bạn có muốn phê duyệt yêu cầu: ${request.title}?")
            .setPositiveButton("Phê duyệt") { _, _ ->
                lifecycleScope.launch {
                    try {
                        showLoading(true)
                        
                        // Cập nhật status sang "In Progress" (2)
                        val result = requestRepository.updateRequestStatus(request.id, 2, "Đã phê duyệt")
                        
                        result.onSuccess {
                            Toast.makeText(this@WorkActivity, "✅ Đã phê duyệt yêu cầu", Toast.LENGTH_SHORT).show()
                            loadMyTasks() // Reload
                        }.onFailure { e ->
                            Toast.makeText(this@WorkActivity, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        showLoading(false)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun rejectRequest(request: Request) {
        AlertDialog.Builder(this)
            .setTitle("Từ chối yêu cầu")
            .setMessage("Bạn có muốn từ chối yêu cầu: ${request.title}?")
            .setPositiveButton("Từ chối") { _, _ ->
                lifecycleScope.launch {
                    try {
                        showLoading(true)
                        
                        // Cập nhật status sang "Closed" (4)
                        val result = requestRepository.updateRequestStatus(request.id, 4, "Đã từ chối")
                        
                        result.onSuccess {
                            Toast.makeText(this@WorkActivity, "❌ Đã từ chối yêu cầu", Toast.LENGTH_SHORT).show()
                            loadMyTasks() // Reload
                        }.onFailure { e ->
                            Toast.makeText(this@WorkActivity, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        showLoading(false)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun completeRequest(request: Request) {
        AlertDialog.Builder(this)
            .setTitle("Hoàn thành yêu cầu")
            .setMessage("Bạn có muốn đánh dấu hoàn thành yêu cầu: ${request.title}?")
            .setPositiveButton("Hoàn thành") { _, _ ->
                lifecycleScope.launch {
                    try {
                        showLoading(true)
                        
                        // Cập nhật status sang "Completed" (3)
                        val result = requestRepository.updateRequestStatus(request.id, 3, "Đã hoàn thành")
                        
                        result.onSuccess {
                            Toast.makeText(this@WorkActivity, "✅ Đã hoàn thành yêu cầu", Toast.LENGTH_SHORT).show()
                            loadMyTasks() // Reload
                        }.onFailure { e ->
                            Toast.makeText(this@WorkActivity, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        showLoading(false)
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
    
    private fun loadMyTasks() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                // Admin thấy TẤT CẢ requests, user thường chỉ thấy requests được assign
                val result = if (currentUserRole?.role == UserRole.ROLE_ADMIN) {
                    requestRepository.getAllRequests()
                } else {
                    requestRepository.getMyTasks()
                }
                
                result.onSuccess { fetchedTasks ->
                    tasks.clear()
                    tasks.addAll(fetchedTasks)
                    taskAdapter.notifyDataSetChanged()
                    
                    tvTaskCount.text = "${tasks.size} công việc"
                }.onFailure { e ->
                    Toast.makeText(
                        this@WorkActivity,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@WorkActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun openTaskDetail(task: Request) {
        val intent = Intent(this, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
        startActivity(intent)
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    // Override navigation methods
    override fun onNavigationHomeClicked() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationWorkClicked() {
        Toast.makeText(this, "Bạn đang ở trang Công việc", Toast.LENGTH_SHORT).show()
        setActiveTab(1)
    }
    
    override fun onNavigationChatClicked() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationAccountClicked() {
        Toast.makeText(this, "Chuyển đến trang Tài khoản", Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh tasks when returning to this activity
        loadMyTasks()
    }
}