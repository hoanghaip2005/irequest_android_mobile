package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RequestsActivity : BaseActivity() {
    
    private lateinit var rvRequests: RecyclerView
    private lateinit var tvRequestCount: TextView
    private lateinit var fabAddRequest: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    
    private val requestRepository = FirebaseRequestRepository()
    private val requests = mutableListOf<Request>()
    private lateinit var requestAdapter: RequestAdapter
    
    private val createRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadRequests()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupBottomNavigation()
        
        loadRequests()
    }
    
    private fun initViews() {
        rvRequests = findViewById(R.id.rvRequests)
        tvRequestCount = findViewById(R.id.tvRequestCount)
        fabAddRequest = findViewById(R.id.fabAddRequest)
        
        // Create progressBar if not exists in layout
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
    }
    
    private fun setupRecyclerView() {
        requestAdapter = RequestAdapter(requests) { request ->
            openRequestDetail(request)
        }
        
        rvRequests.apply {
            layoutManager = LinearLayoutManager(this@RequestsActivity)
            adapter = requestAdapter
        }
    }
    
    private fun setupListeners() {
        fabAddRequest.setOnClickListener {
            val intent = Intent(this, CreateRequestActivity::class.java)
            createRequestLauncher.launch(intent)
        }
    }
    
    private fun loadRequests() {
        showLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    requestRepository.getMyRequests()
                }
                
                result.onSuccess { fetchedRequests ->
                    requests.clear()
                    requests.addAll(fetchedRequests)
                    requestAdapter.notifyDataSetChanged()
                    
                    tvRequestCount.text = "${requests.size} yêu cầu"
                }.onFailure { e ->
                    Toast.makeText(
                        this@RequestsActivity,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RequestsActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun openRequestDetail(request: Request) {
        val intent = Intent(this, RequestDetailActivity::class.java)
        intent.putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, request.id)
        startActivity(intent)
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        fabAddRequest.isEnabled = !show
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
