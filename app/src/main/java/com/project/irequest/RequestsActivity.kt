package com.project.irequest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project.irequest.data.repository.RequestRepository
import kotlinx.coroutines.launch

class RequestsActivity : BaseActivity() {
    
    private lateinit var requestRepository: RequestRepository
    private lateinit var rvRequests: RecyclerView
    private lateinit var fabAddRequest: FloatingActionButton

    companion object {
        private const val TAG = "RequestsActivity"
        private const val REQUEST_CREATE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        // Initialize repository
        requestRepository = RequestRepository()
        
        initViews()
        setupRecyclerView()
        loadRequests()
        
        // Setup bottom navigation
        setupBottomNavigation()
    }
    
    private fun initViews() {
        rvRequests = findViewById<RecyclerView?>(R.id.rvRequests) ?: return
        fabAddRequest = findViewById<FloatingActionButton?>(R.id.fabAddRequest) ?: return
        
        fabAddRequest.setOnClickListener {
            // Open create request activity
            val intent = android.content.Intent(this, CreateRequestActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE)
        }
    }
    
    private fun setupRecyclerView() {
        rvRequests.layoutManager = LinearLayoutManager(this)
        // Adapter will be set after loading data
    }
    
    private fun loadRequests() {
        lifecycleScope.launch {
            requestRepository.getMyRequests().onSuccess { requests ->
                runOnUiThread {
                    if (requests.isEmpty()) {
                        Toast.makeText(
                            this@RequestsActivity,
                            "Bạn chưa có yêu cầu nào",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // TODO: Setup adapter with requests
                        Toast.makeText(
                            this@RequestsActivity,
                            "Đã tải ${requests.size} yêu cầu",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        Log.d(TAG, "Loaded ${requests.size} requests")
                        requests.forEach { request ->
                            Log.d(TAG, "Request: ${request.title}")
                        }
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading requests", error)
                runOnUiThread {
                    Toast.makeText(
                        this@RequestsActivity,
                        "Lỗi tải dữ liệu: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CREATE && resultCode == android.app.Activity.RESULT_OK) {
            // Reload requests after creating a new one
            loadRequests()
        }
    }

    override fun onNavigationHomeClicked() {
        val intent = android.content.Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationWorkClicked() {
        val intent = android.content.Intent(this, WorkActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationChatClicked() {
        val intent = android.content.Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationAccountClicked() {
        android.widget.Toast.makeText(this, "Chuyển đến trang Tài khoản", android.widget.Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
}
