package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class HomeActivity : BaseActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupLogoutButton()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Home là active (index 0)
        setActiveTab(0)
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
        // TODO: Chuyển đến AccountActivity
        Toast.makeText(this, "Chuyển đến trang Tài khoản", Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
}