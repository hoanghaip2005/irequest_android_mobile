package com.project.irequest

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
        
        // Trang chủ
        val navHome: LinearLayout = findViewById(R.id.navHome)
        navHome.setOnClickListener {
            onNavigationHomeClicked()
        }
        
        // Công việc  
        val navWork: LinearLayout = findViewById(R.id.navWork)
        navWork.setOnClickListener {
            onNavigationWorkClicked()
        }
        
        // Chat
        val navChat: LinearLayout = findViewById(R.id.navChat)
        navChat.setOnClickListener {
            onNavigationChatClicked()
        }
        
        // Tài khoản
        val navAccount: LinearLayout = findViewById(R.id.navAccount)
        navAccount.setOnClickListener {
            onNavigationAccountClicked()
        }
    }
    
    // Các method này có thể được override bởi activity con
    protected open fun onNavigationHomeClicked() {
        Toast.makeText(this, "Trang chủ clicked", Toast.LENGTH_SHORT).show()
    }
    
    protected open fun onNavigationWorkClicked() {
        Toast.makeText(this, "Công việc clicked", Toast.LENGTH_SHORT).show()
    }
    
    protected open fun onNavigationChatClicked() {
        Toast.makeText(this, "Chat clicked", Toast.LENGTH_SHORT).show()
    }
    
    protected open fun onNavigationAccountClicked() {
        Toast.makeText(this, "Tài khoản clicked", Toast.LENGTH_SHORT).show()
    }
    
    // Method để highlight tab hiện tại
    protected fun setActiveTab(tabIndex: Int) {
        // Reset tất cả tab về màu normal
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
            val tab: LinearLayout = findViewById(tabId)
            tab.alpha = 0.6f
        }
    }
    
    private fun highlightTab(tabId: Int) {
        val tab: LinearLayout = findViewById(tabId)
        tab.alpha = 1.0f
    }
}