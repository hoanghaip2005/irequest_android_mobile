package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.FirebaseChatRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ChatActivity : BaseActivity() {

    private lateinit var rvChatList: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var fabNewChat: FloatingActionButton
    
    private val chatRepository = FirebaseChatRepository()
    
    private val selectUserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val userId = data?.getStringExtra("USER_ID")
            val userName = data?.getStringExtra("USER_NAME")
            
            if (userId != null && userName != null) {
                createChatWithUser(userId, userName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setupViews()
        loadChatsFromFirebase()
        
        setupBottomNavigation()
        setActiveTab(2)
    }
    
    private fun setupViews() {
        rvChatList = findViewById(R.id.rvChatList)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabNewChat = findViewById(R.id.fabNewChat)
        
        rvChatList.layoutManager = LinearLayoutManager(this)
        
        fabNewChat.setOnClickListener {
            val intent = Intent(this, SelectUserActivity::class.java)
            selectUserLauncher.launch(intent)
        }
    }
    
    private fun createChatWithUser(userId: String, userName: String) {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val result = chatRepository.getOrCreateDirectChat(userId, userName)
                
                result.onSuccess { chatId ->
                    progressBar.visibility = View.GONE
                    
                    // Open chat detail
                    val intent = Intent(this@ChatActivity, ChatDetailActivity::class.java)
                    intent.putExtra("CHAT_ID", chatId)
                    intent.putExtra("CHAT_NAME", userName)
                    intent.putExtra("RECEIVER_ID", userId)
                    intent.putExtra("RECEIVER_NAME", userName)
                    startActivity(intent)
                    
                    // Reload chats
                    loadChatsFromFirebase()
                }
                
                result.onFailure { error ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ChatActivity,
                        "Lỗi tạo chat: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ChatActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun loadChatsFromFirebase() {
        progressBar.visibility = View.VISIBLE
        rvChatList.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                // Get chats from Firestore
                val result = chatRepository.getUserChats()
                
                result.onSuccess { chats ->
                    progressBar.visibility = View.GONE
                    
                    if (chats.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text = "Chưa có cuộc trò chuyện nào\nNhấn + để bắt đầu chat"
                        rvChatList.visibility = View.GONE
                    } else {
                        rvChatList.visibility = View.VISIBLE
                        tvEmpty.visibility = View.GONE
                        
                        // Convert Chat to ChatItem
                        val chatItems = chats.map { chat ->
                            // For direct chats, show the other user's name
                            val displayName = if (chat.type == "user") {
                                chat.otherUserName ?: chat.userName ?: "Unknown"
                            } else {
                                chat.groupName ?: "Group Chat"
                            }
                            
                            ChatItem(
                                chatId = chat.id,
                                name = displayName,
                                message = chat.lastMessage ?: "Chưa có tin nhắn",
                                timestampMillis = chat.lastMessageTime?.time ?: System.currentTimeMillis(),
                                unreadCount = chat.unreadCount,
                                avatarResId = R.drawable.ic_launcher_background,
                                receiverId = chat.otherUserId ?: chat.userId,
                                receiverName = chat.otherUserName ?: chat.userName
                            )
                        }
                        
                        chatAdapter = ChatAdapter(chatItems)
                        rvChatList.adapter = chatAdapter
                    }
                }
                
                result.onFailure { error ->
                    progressBar.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Lỗi: ${error.message}"
                    Toast.makeText(
                        this@ChatActivity,
                        "Lỗi tải dữ liệu: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Lỗi: ${e.message}"
                Toast.makeText(
                    this@ChatActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
        }
    }

    override fun onNavigationChatClicked() {
        // Đã ở đây rồi
    }
}