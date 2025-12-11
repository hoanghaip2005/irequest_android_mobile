package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    
    private lateinit var tvTotalRequests: TextView
    private lateinit var tvPendingRequests: TextView
    private lateinit var tvCompletedRequests: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        // Initialize TextViews
        tvTotalRequests = findViewById(R.id.tvTotalRequests)
        tvPendingRequests = findViewById(R.id.tvPendingRequests)
        tvCompletedRequests = findViewById(R.id.tvCompletedRequests)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = "Xin chào, Hoàng Hải!"

        setupFeatureCards()
        
        setupBottomNavigation()
        
        setActiveTab(0)
        
        // Load request statistics
        loadRequestStatistics()
    }
    
    private fun loadRequestStatistics() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("HomeActivity", "User not logged in")
            return
        }
        
        // Query requests for current user
        firestore.collection("requests")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                var totalCount = 0
                var pendingCount = 0
                var completedCount = 0
                
                for (document in documents) {
                    totalCount++
                    val status = document.getString("status") ?: ""
                    
                    when (status.lowercase()) {
                        "pending", "đang xử lý", "processing" -> pendingCount++
                        "completed", "hoàn thành", "done" -> completedCount++
                    }
                }
                
                // Update UI
                tvTotalRequests.text = totalCount.toString()
                tvPendingRequests.text = pendingCount.toString()
                tvCompletedRequests.text = completedCount.toString()
                
                Log.d("HomeActivity", "Statistics loaded: Total=$totalCount, Pending=$pendingCount, Completed=$completedCount")
            }
            .addOnFailureListener { exception ->
                Log.e("HomeActivity", "Error loading statistics", exception)
                Toast.makeText(this, "Không thể tải thống kê", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFeatureCards() {
        setClickListener(R.id.cardCalendar, com.project.irequest.CalenderActivity::class.java)
        setClickListener(R.id.cardNotification, com.project.irequest.AlertsActivity::class.java)
        setClickListener(R.id.cardRequest, com.project.irequest.RequestsActivity::class.java)
        setClickListener(R.id.cardReport, com.project.irequest.ReportActivity::class.java)
        setClickListener(R.id.cardEmployee, com.project.irequest.EmployeeActivity::class.java)
        setClickListener(R.id.cardDepartment, com.project.irequest.DepartmentActivity::class.java)
        setClickListener(R.id.cardProcess, com.project.irequest.ProcessManagementActivity::class.java)
        // cardProcessStep - will be opened from ProcessManagementActivity when user selects a process
        setClickListener(R.id.cardProcessStep, com.project.irequest.ProcessManagementActivity::class.java)
        setClickListener(R.id.cardPayment, com.project.irequest.PaymentActivity::class.java)
    }

    private fun setClickListener(@IdRes viewId: Int, activityClass: Class<*>) {
        findViewById<View>(viewId).setOnClickListener {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        }
    }
    
    private fun setToastClickListener(@IdRes viewId: Int, message: String) {
        findViewById<View>(viewId).setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Override navigation methods
    override fun onNavigationHomeClicked() {
        Toast.makeText(this, "Bạn đang ở trang chủ", Toast.LENGTH_SHORT).show()
        setActiveTab(0)
    }
    
    override fun onNavigationWorkClicked() {
        val intent = Intent(this, com.project.irequest.WorkActivity::class.java)
        startActivity(intent)
    }
    
    override fun onNavigationChatClicked() {
        val intent = Intent(this, com.project.irequest.ChatActivity::class.java)
        startActivity(intent)
    }
    
    override fun onNavigationAccountClicked() {
        val intent = Intent(this, com.project.irequest.AccountActivity::class.java)
        startActivity(intent)
    }
}
