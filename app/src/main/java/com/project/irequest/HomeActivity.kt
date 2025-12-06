package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupFeatureCards()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Home là active (index 0)
        setActiveTab(0)
    }

    private fun setupFeatureCards() {
        // Đặt lịch
        findViewById<LinearLayout>(R.id.cardCalendar).setOnClickListener {
            val intent = Intent(this, CalenderActivity::class.java)
            startActivity(intent)
        }
        
        // Thông báo
        findViewById<LinearLayout>(R.id.cardNotification).setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            startActivity(intent)
        }
        
        // Nộp phí
        findViewById<LinearLayout>(R.id.cardPayment).setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
        }

        // Quản lý quy trình
        findViewById<LinearLayout>(R.id.cardProcess).setOnClickListener {
            val intent = Intent(this, ProcessManagementActivity::class.java)
            startActivity(intent)
        }
        
        // TODO: Setup click listeners cho các feature cards khác
        // findViewById<LinearLayout>(R.id.cardRequest).setOnClickListener { }
        // ...
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