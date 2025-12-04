package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView

class HomeActivity : BaseActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button
    private lateinit var cardRequests: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupLogoutButton()
        setupRequestsCard()
        setupFeatureCards()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Home là active (index 0)
        setActiveTab(0)
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
        cardRequests = findViewById(R.id.cardRequests)
    }

    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun setupFeatureCards() {
        // Đặt lịch
        findViewById<android.widget.LinearLayout>(R.id.cardCalendar).setOnClickListener {
            val intent = Intent(this, CalenderActivity::class.java)
            startActivity(intent)
        }
        
        // Thông báo
        findViewById<android.widget.LinearLayout>(R.id.cardNotification).setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            startActivity(intent)
        }
        
        // Nộp phí
        findViewById<android.widget.LinearLayout>(R.id.cardPayment).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRequestsCard() {
        cardRequests.setOnClickListener {
            val intent = Intent(this, RequestsActivity::class.java)
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
