package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class WorkActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work)
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Work là active (index 1)
        setActiveTab(1)
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