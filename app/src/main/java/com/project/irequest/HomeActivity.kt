package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.project.irequest.data.repository.RequestRepository
import com.project.irequest.data.repository.UserRepository
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var requestRepository: RequestRepository
    
    // UI Elements
    private lateinit var tvUserName: TextView
    private lateinit var tvTotalRequests: TextView
    private lateinit var tvPendingRequests: TextView
    private lateinit var tvCompletedRequests: TextView

    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        requestRepository = RequestRepository()
        
        initViews()
        loadUserData()
        loadStatistics()
        setupFeatureCards()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Home là active (index 0)
        setActiveTab(0)
    }
    
    private fun initViews() {
        // Initialize views if they exist in layout
        tvUserName = findViewById<TextView?>(R.id.tvUserName) ?: return
        tvTotalRequests = findViewById<TextView?>(R.id.tvTotalRequests) ?: return
        tvPendingRequests = findViewById<TextView?>(R.id.tvPendingRequests) ?: return
        tvCompletedRequests = findViewById<TextView?>(R.id.tvCompletedRequests) ?: return
    }
    
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                userRepository.getCurrentUser().onSuccess { user ->
                    runOnUiThread {
                        if (user != null) {
                            tvUserName.text = "Xin chào, ${user.userName}"
                        } else {
                            tvUserName.text = "Xin chào, ${currentUser.displayName ?: currentUser.email}"
                        }
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Error loading user data", error)
                    runOnUiThread {
                        tvUserName.text = "Xin chào, ${currentUser.displayName ?: currentUser.email}"
                    }
                }
            }
        } else {
            tvUserName.text = "Xin chào"
        }
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            requestRepository.getMyRequests().onSuccess { requests ->
                runOnUiThread {
                    tvTotalRequests.text = requests.size.toString()
                    
                    val pending = requests.count { !it.isApproved && it.closedAt == null }
                    tvPendingRequests.text = pending.toString()
                    
                    val completed = requests.count { it.closedAt != null }
                    tvCompletedRequests.text = completed.toString()
                }
            }.onFailure { error ->
                Log.e(TAG, "Error loading statistics", error)
                runOnUiThread {
                    tvTotalRequests.text = "0"
                    tvPendingRequests.text = "0"
                    tvCompletedRequests.text = "0"
                }
            }
        }
    }

    private fun setupFeatureCards() {
        // 1. Đặt lịch
        findViewById<android.widget.LinearLayout>(R.id.cardCalendar).setOnClickListener {
            val intent = Intent(this, CalenderActivity::class.java)
            startActivity(intent)
        }
        
        // 2. Thông báo
        findViewById<android.widget.LinearLayout>(R.id.cardNotification).setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            startActivity(intent)
        }
        
        // 3. Yêu cầu
        findViewById<android.widget.LinearLayout>(R.id.cardRequest).setOnClickListener {
            val intent = Intent(this, RequestsActivity::class.java)
            startActivity(intent)
        }
        
        // 4. Báo cáo
        findViewById<android.widget.LinearLayout>(R.id.cardReport).setOnClickListener {
            Toast.makeText(this, "Tính năng Báo cáo đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 5. Nhân viên
        findViewById<android.widget.LinearLayout>(R.id.cardEmployee).setOnClickListener {
            Toast.makeText(this, "Tính năng Nhân viên đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 6. Phòng ban
        findViewById<android.widget.LinearLayout>(R.id.cardDepartment).setOnClickListener {
            Toast.makeText(this, "Tính năng Phòng ban đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 7. Quy trình
        findViewById<android.widget.LinearLayout>(R.id.cardProcess).setOnClickListener {
            Toast.makeText(this, "Tính năng Quy trình đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 8. Bước quy trình
        findViewById<android.widget.LinearLayout>(R.id.cardProcessStep).setOnClickListener {
            Toast.makeText(this, "Tính năng Bước quy trình đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 9. Quyền
        findViewById<android.widget.LinearLayout>(R.id.cardPermission).setOnClickListener {
            Toast.makeText(this, "Tính năng Quyền đang phát triển", Toast.LENGTH_SHORT).show()
        }
        
        // 10. Nộp phí
        findViewById<android.widget.LinearLayout>(R.id.cardPayment).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

        // 11. Báo cáo
        // (Đây là chỗ nối 2 màn hình lại với nhau)
        findViewById<android.view.View>(R.id.cardReport).setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }
    }

    // Override navigation methods để xử lý riêng cho HomeActivity
    override fun onNavigationHomeClicked() {
        // Đã ở trang chủ rồi, không cần làm gì
        Toast.makeText(this, "Bạn đang ở trang chủ", Toast.LENGTH_SHORT).show()
        setActiveTab(0)
    }
    
    override fun onNavigationWorkClicked() {
        // Chuyển đến WorkActivity
        val intent = Intent(this, WorkActivity::class.java)
        startActivity(intent)
    }
    
    override fun onNavigationChatClicked() {
        // Chuyển đến ChatActivity
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }
    
    override fun onNavigationAccountClicked() {
        // Chuyển đến AccountActivity
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }
}
