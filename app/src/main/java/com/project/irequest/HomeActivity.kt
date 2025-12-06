package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView

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
