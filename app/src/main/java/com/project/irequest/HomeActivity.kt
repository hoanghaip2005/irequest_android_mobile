package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.IdRes
import com.project.irequest.BaseActivity
import com.project.irequest.CalenderActivity
import com.project.irequest.AlertsActivity
import com.project.irequest.RequestsActivity
import com.project.irequest.ReportActivity
import com.project.irequest.DepartmentActivity
import com.project.irequest.ProcessManagementActivity
import com.project.irequest.PaymentActivity
import com.project.irequest.WorkActivity
import com.project.irequest.ChatActivity
import com.project.irequest.AccountActivity

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
        // Sử dụng hàm phụ trợ để code gọn hơn
        setClickListener(R.id.cardCalendar, CalenderActivity::class.java)
        setClickListener(R.id.cardNotification, AlertsActivity::class.java)
        setClickListener(R.id.cardRequest, RequestsActivity::class.java)
        setClickListener(R.id.cardReport, ReportActivity::class.java)
        setClickListener(R.id.cardDepartment, DepartmentActivity::class.java)
        setClickListener(R.id.cardProcess, ProcessManagementActivity::class.java) // <-- Logic quy trình ở đây
        setClickListener(R.id.cardPayment, PaymentActivity::class.java)

        // Các tính năng đang phát triển
        setToastClickListener(R.id.cardEmployee, "Tính năng Nhân viên đang phát triển")
        setToastClickListener(R.id.cardProcessStep, "Tính năng Bước quy trình đang phát triển")
        setToastClickListener(R.id.cardPermission, "Tính năng Quyền đang phát triển")
    }

    /**
     * Hàm phụ trợ để gán sự kiện click mở một Activity mới.
     * @param viewId ID của View cần gán sự kiện.
     * @param activityClass Class của Activity cần mở.
     */
    private fun setClickListener(@IdRes viewId: Int, activityClass: Class<*>) {
        findViewById<View>(viewId).setOnClickListener {
            val intent = Intent(this, activityClass)
            startActivity(intent)
        }
    }
    
    /**
     * Hàm phụ trợ để gán sự kiện click hiển thị Toast.
     * @param viewId ID của View cần gán sự kiện.
     * @param message Nội dung Toast.
     */
    private fun setToastClickListener(@IdRes viewId: Int, message: String) {
        findViewById<View>(viewId).setOnClickListener {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
