package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class ChatActivity : BaseActivity() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Thiết lập RecyclerView
        rvChatList = findViewById(R.id.rvChatList)
        rvChatList.layoutManager = LinearLayoutManager(this)

        // Dữ liệu mẫu với timestamp
        val now = System.currentTimeMillis()
        val chatItems = listOf(
            ChatItem("Thành viên nhóm", "Bạn: Chào bạn!", now - TimeUnit.MINUTES.toMillis(1), 1, R.drawable.ic_launcher_background),
            ChatItem("Hỗ trợ kỹ thuật", "Cảm ơn bạn đã liên hệ.", now - TimeUnit.HOURS.toMillis(1), 0, R.drawable.ic_launcher_background),
            ChatItem("Thông báo hệ thống", "Hệ thống sẽ bảo trì vào lúc 2 giờ sáng.", now - TimeUnit.DAYS.toMillis(1), 0, R.drawable.ic_launcher_background)
        )

        chatAdapter = ChatAdapter(chatItems)
        rvChatList.adapter = chatAdapter

        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Chat là active (index 2)
        setActiveTab(2)
    }

    override fun onNavigationHomeClicked() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationWorkClicked() {
        val intent = Intent(this, WorkActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onNavigationChatClicked() {
        // Đã ở trang Chat rồi
        Toast.makeText(this, "Bạn đang ở trang Chat", Toast.LENGTH_SHORT).show()
        setActiveTab(2)
    }
    
    override fun onNavigationAccountClicked() {
        // TODO: Chuyển đến AccountActivity
        Toast.makeText(this, "Chuyển đến trang Tài khoản", Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
}
