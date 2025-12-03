package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: Button

    // Khai báo biến cho card thông báo
    private lateinit var cardAlerts: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupLogoutButton()
        setupNavigation()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)

        // Tìm View theo ID đã thêm trong XML
        cardAlerts = findViewById(R.id.cardAlerts)
    }

    private fun setupLogoutButton() {
        btnLogout.setOnClickListener {
            // Chuyển về màn hình đăng nhập
            val intent = Intent(this, LoginActivity::class.java)
            // Xóa back stack để user không back lại được Home
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupNavigation() {
        // Sự kiện click vào thẻ Thông báo
        cardAlerts.setOnClickListener {
            val intent = Intent(this, AlertsActivity::class.java)
            startActivity(intent)
        }
    }
}