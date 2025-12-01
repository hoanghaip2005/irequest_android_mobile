package com.project.irequest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

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
    }
}