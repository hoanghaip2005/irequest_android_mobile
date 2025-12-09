package com.project.irequest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.FirebaseChatRepository
import kotlinx.coroutines.launch

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    
    private val messages = mutableListOf<ChatMessage>()
    private val chatRepository = FirebaseChatRepository()
    
    private var chatId: String? = null
    private var receiverId: String? = null
    private var receiverName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val tvToolbarTitle: TextView = findViewById(R.id.tv_toolbar_title)
        val ivBackArrow: ImageView = findViewById(R.id.iv_back_arrow)
        val ivAvatar: ImageView = findViewById(R.id.iv_avatar)

        // Nhận dữ liệu từ Intent
        val chatName = intent.getStringExtra("CHAT_NAME") ?: "Chat"
        val avatarResId = intent.getIntExtra("AVATAR_RES_ID", R.drawable.ic_launcher_background)
        chatId = intent.getStringExtra("CHAT_ID")
        receiverId = intent.getStringExtra("RECEIVER_ID")
        receiverName = intent.getStringExtra("RECEIVER_NAME")

        tvToolbarTitle.text = chatName
        ivAvatar.setImageResource(avatarResId)

        ivBackArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupViews()
        
        if (chatId != null) {
            loadMessagesFromFirebase(chatId!!)
        } else {
              Toast.makeText(this, "Lỗi: Không có chatId", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupViews() {
        rvMessages = findViewById(R.id.rv_chat_messages)
        progressBar = findViewById(R.id.progressBar)
        etMessageInput = findViewById(R.id.et_message_input)
        btnSend = findViewById(R.id.btn_send)
        
        rvMessages.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messages)
        rvMessages.adapter = messageAdapter
        
        btnSend.setOnClickListener {
            sendMessage()
        }
    }
    
    private fun loadMessagesFromFirebase(chatId: String) {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = chatRepository.getChatMessages(chatId, 100)
                
                result.onSuccess { firebaseMessages ->
                    progressBar.visibility = View.GONE
                    
                    if (firebaseMessages.isEmpty()) {
                        // No messages - empty state
                        messages.clear()
                        messageAdapter.notifyDataSetChanged()
                    } else {
                        messages.clear()
                        
                        // Convert Message to ChatMessage
                        firebaseMessages.forEach { msg ->
                            val isSent = msg.senderId == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            messages.add(ChatMessage(msg.content, isSent))
                        }
                        
                        messageAdapter.notifyDataSetChanged()
                        rvMessages.scrollToPosition(messages.size - 1)
                        
                        // Mark messages as read
                        chatRepository.markChatMessagesAsRead(chatId)
                    }
                }
                
                result.onFailure { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ChatDetailActivity,
                        "Lỗi tải tin nhắn: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ChatDetailActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun sendMessage() {
        val messageText = etMessageInput.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (chatId == null) {
            Toast.makeText(this, "Lỗi: Không có chatId", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Send to Firebase
        btnSend.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val result = chatRepository.sendMessage(
                    chatId = chatId!!,
                    content = messageText,
                    receiverId = receiverId,
                    receiverName = receiverName
                )
                
                result.onSuccess {
                    // Add to UI
                    val newMessage = ChatMessage(messageText, true)
                    messages.add(newMessage)
                    messageAdapter.notifyItemInserted(messages.size - 1)
                    rvMessages.scrollToPosition(messages.size - 1)
                    etMessageInput.text.clear()
                }
                
                result.onFailure { error ->
                    Toast.makeText(
                        this@ChatDetailActivity,
                        "Lỗi gửi tin nhắn: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChatDetailActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                btnSend.isEnabled = true
            }
        }
    }
}