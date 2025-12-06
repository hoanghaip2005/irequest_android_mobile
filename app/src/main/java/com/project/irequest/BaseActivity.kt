package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    protected lateinit var bottomNavigation: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    // Setup navigation cho các activity con
    protected fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        val navHome: LinearLayout = findViewById(R.id.navHome)
        navHome.setOnClickListener {
            onNavigationHomeClicked()
        }
        
        val navWork: LinearLayout = findViewById(R.id.navWork)
        navWork.setOnClickListener {
            onNavigationWorkClicked()
        }
        
        val navChat: LinearLayout = findViewById(R.id.navChat)
        navChat.setOnClickListener {
            onNavigationChatClicked()
        }
        
        val navAccount: LinearLayout = findViewById(R.id.navAccount)
        navAccount.setOnClickListener {
            onNavigationAccountClicked()
        }
    }
    
    // Điều hướng đến HomeActivity, tránh tạo lại activity mới
    protected open fun onNavigationHomeClicked() {
        if (this !is HomeActivity) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }
    
    protected open fun onNavigationWorkClicked() {
        Toast.makeText(this, "Công việc clicked", Toast.LENGTH_SHORT).show()
    }
    
    // Điều hướng đến ChatActivity, tránh tạo lại activity mới
    protected open fun onNavigationChatClicked() {
        if (this !is ChatActivity) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }
    
    protected open fun onNavigationAccountClicked() {
        Toast.makeText(this, "Tài khoản clicked", Toast.LENGTH_SHORT).show()
    }
    
    // Method để highlight tab hiện tại
    protected fun setActiveTab(tabIndex: Int) {
        if (!::bottomNavigation.isInitialized) return
        resetTabColors()
        
        when (tabIndex) {
            0 -> highlightTab(R.id.navHome)
            1 -> highlightTab(R.id.navWork) 
            2 -> highlightTab(R.id.navChat)
            3 -> highlightTab(R.id.navAccount)
        }
    }
    
    private fun resetTabColors() {
        val tabs = listOf(R.id.navHome, R.id.navWork, R.id.navChat, R.id.navAccount)
        for (tabId in tabs) {
            findViewById<LinearLayout?>(tabId)?.alpha = 0.6f
        }
    }
    
    private fun highlightTab(tabId: Int) {
        findViewById<LinearLayout?>(tabId)?.alpha = 1.0f
    }
}