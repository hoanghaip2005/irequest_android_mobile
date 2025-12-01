package com.project.irequest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar_chat_detail)
        setSupportActionBar(toolbar)

        val chatName = intent.getStringExtra("CHAT_NAME")
        supportActionBar?.title = chatName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // --- Thiết lập RecyclerView ---
        rvMessages = findViewById(R.id.rv_chat_messages)
        rvMessages.layoutManager = LinearLayoutManager(this)

        // Thêm dữ liệu trò chuyện mẫu
        addSampleMessages()

        messageAdapter = MessageAdapter(messages)
        rvMessages.adapter = messageAdapter
        rvMessages.scrollToPosition(messages.size - 1) // Cuộn xuống tin nhắn cuối cùng

        // --- Xử lý gửi tin nhắn ---
        val etMessageInput: EditText = findViewById(R.id.et_message_input)
        val btnSend: Button = findViewById(R.id.btn_send)

        btnSend.setOnClickListener {
            val messageText = etMessageInput.text.toString()
            if (messageText.isNotEmpty()) {
                val newMessage = ChatMessage(messageText, true) // Tin nhắn mới luôn được gửi đi
                messages.add(newMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                rvMessages.scrollToPosition(messages.size - 1) // Cuộn xuống khi có tin nhắn mới
                etMessageInput.text.clear()
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addSampleMessages() {
        messages.add(ChatMessage("Chào bạn, tôi có thể giúp gì cho bạn?", false))
        messages.add(ChatMessage("Tôi đang gặp sự cố với tài khoản của mình.", true))
        messages.add(ChatMessage("Vui lòng cho tôi biết chi tiết sự cố.", false))
        messages.add(ChatMessage("Tôi không thể đăng nhập được.", true))
        messages.add(ChatMessage("Bạn đã thử đặt lại mật khẩu chưa?", false))
        messages.add(ChatMessage("Rồi, nhưng không có tác dụng.", true))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}