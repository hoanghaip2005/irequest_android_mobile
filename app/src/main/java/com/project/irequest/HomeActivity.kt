package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes

class HomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        tvUserName.text = "Xin chào, Hoàng Hải!"

        setupFeatureCards()
        
        setupBottomNavigation()
        
        setActiveTab(0)
    }

    private fun setupFeatureCards() {
        setClickListener(R.id.cardCalendar, com.project.irequest.CalendarActivity::class.java)
        setClickListener(R.id.cardNotification, com.project.irequest.AlertsActivity::class.java)
        setClickListener(R.id.cardRequest, com.project.irequest.RequestsActivity::class.java)
        setClickListener(R.id.cardReport, com.project.irequest.ReportActivity::class.java)
        setClickListener(R.id.cardDepartment, com.project.irequest.DepartmentActivity::class.java)
        setClickListener(R.id.cardProcess, com.project.irequest.ProcessManagementActivity::class.java)
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
