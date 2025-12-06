package com.project.irequest

import android.os.Bundle

class RequestsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        // Setup bottom navigation
        setupBottomNavigation()
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
