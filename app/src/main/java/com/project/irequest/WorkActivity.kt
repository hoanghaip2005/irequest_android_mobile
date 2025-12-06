package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.irequest.data.repository.RequestRepository
import kotlinx.coroutines.launch

class WorkActivity : BaseActivity() {
    
    private lateinit var requestRepository: RequestRepository
    private lateinit var rvTasks: RecyclerView

    companion object {
        private const val TAG = "WorkActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        
        // Initialize repository
        requestRepository = RequestRepository()
        
        initViews()
        setupRecyclerView()
        loadMyTasks()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Work là active (index 1)
        setActiveTab(1)
    }
    
    private fun initViews() {
        rvTasks = findViewById<RecyclerView?>(R.id.rvTasks) ?: return
    }
    
    private fun setupRecyclerView() {
        rvTasks.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadMyTasks() {
        lifecycleScope.launch {
            requestRepository.getMyTasks().onSuccess { tasks ->
                runOnUiThread {
                    if (tasks.isEmpty()) {
                        Toast.makeText(
                            this@WorkActivity,
                            "Bạn chưa có công việc nào được giao",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@WorkActivity,
                            "Đã tải ${tasks.size} công việc",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        Log.d(TAG, "Loaded ${tasks.size} tasks")
                        tasks.forEach { task ->
                            Log.d(TAG, "Task: ${task.title}")
                        }
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading tasks", error)
                runOnUiThread {
                    Toast.makeText(
                        this@WorkActivity,
                        "Lỗi tải dữ liệu: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    // Override navigation methods
    override fun onNavigationHomeClicked() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationWorkClicked() {
        // Đã ở trang công việc rồi
        Toast.makeText(this, "Bạn đang ở trang Công việc", Toast.LENGTH_SHORT).show()
        setActiveTab(1)
    }
    
    override fun onNavigationChatClicked() {
        // TODO: Chuyển đến ChatActivity
        Toast.makeText(this, "Chuyển đến trang Chat", Toast.LENGTH_SHORT).show()
        setActiveTab(2)
    }
    
    override fun onNavigationAccountClicked() {
        // TODO: Chuyển đến AccountActivity
        Toast.makeText(this, "Chuyển đến trang Tài khoản", Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
}