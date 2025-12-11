package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.FirebaseChatRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: Button
    private lateinit var btnAddImage: ImageView
    
    private val messages = mutableListOf<ChatMessage>()
    private val chatRepository = FirebaseChatRepository()
    private val storage = FirebaseStorage.getInstance()
    
    private var chatId: String? = null
    private var sharedChatId: String? = null
    private var receiverId: String? = null
    private var receiverName: String? = null
    private var selectedImageUri: Uri? = null
    
    private var messagesListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                uploadAndSendImage(uri)
            }
        }
    }

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
        sharedChatId = intent.getStringExtra("SHARED_CHAT_ID")
        receiverId = intent.getStringExtra("RECEIVER_ID")
        receiverName = intent.getStringExtra("RECEIVER_NAME")

        tvToolbarTitle.text = chatName
        ivAvatar.setImageResource(avatarResId)

        ivBackArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupViews()
        
        // Sử dụng sharedChatId để lắng nghe tin nhắn (cả 2 user cùng sharedChatId)
        // Nếu sharedChatId null (chat cũ), tạo sharedChatId từ receiverId
        val chatIdToUse = if (sharedChatId != null) {
            sharedChatId
        } else {
            val localReceiverId = receiverId
            if (localReceiverId != null) {
                // Generate sharedChatId từ currentUserId và receiverId
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    if (currentUserId < localReceiverId) {
                        "chat_${currentUserId}_${localReceiverId}"
                    } else {
                        "chat_${localReceiverId}_${currentUserId}"
                    }
                } else {
                    chatId
                }
            } else {
                chatId
            }
        }
        
        android.util.Log.d("ChatDetail", "Using chatIdToUse: $chatIdToUse (sharedChatId=$sharedChatId, chatId=$chatId, receiverId=$receiverId)")
        
        if (chatIdToUse != null) {
            setupRealtimeMessagesListener(chatIdToUse)
        } else {
              Toast.makeText(this, "Lỗi: Không có chatId", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        messagesListener?.remove()
    }
    
    private fun setupViews() {
        rvMessages = findViewById(R.id.rv_chat_messages)
        progressBar = findViewById(R.id.progressBar)
        etMessageInput = findViewById(R.id.et_message_input)
        btnSend = findViewById(R.id.btn_send)
        btnAddImage = findViewById(R.id.btnAddImage)
        
        rvMessages.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messages)
        rvMessages.adapter = messageAdapter
        
        btnSend.setOnClickListener {
            sendMessage()
        }
        
        btnAddImage.setOnClickListener {
            openImagePicker()
        }
    }
    
    private fun setupRealtimeMessagesListener(chatId: String) {
        progressBar.visibility = View.VISIBLE
        
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        
        android.util.Log.d("ChatDetail", "Setting up listener for groupId: $chatId, currentUser: $currentUserId")
        
        // Listen to messages in real-time
        messagesListener = firestore.collection("messages")
            .whereEqualTo("groupId", chatId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ChatDetail", "Listen failed", error)
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@ChatDetailActivity,
                        "Lỗi tải tin nhắn: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    progressBar.visibility = View.GONE
                    messages.clear()
                    
                    android.util.Log.d("ChatDetail", "Received ${snapshot.documents.size} message documents")
                    
                    // Convert and sort messages
                    val firebaseMessages = snapshot.documents.mapNotNull { doc ->
                        val msg = doc.toObject(com.example.irequest.data.models.Message::class.java)
                        android.util.Log.d("ChatDetail", "Message: groupId=${msg?.groupId}, senderId=${msg?.senderId}, content=${msg?.content}")
                        msg
                    }.sortedBy { it.createdAt?.time ?: 0L }
                    
                    firebaseMessages.forEach { msg ->
                        val isSent = msg.senderId == currentUserId
                        val messageType = when {
                            msg.imageUrl != null && msg.content.isNotEmpty() -> MessageType.TEXT_WITH_IMAGE
                            msg.imageUrl != null -> MessageType.IMAGE
                            else -> MessageType.TEXT
                        }
                        messages.add(ChatMessage(msg.content, isSent, msg.imageUrl, messageType))
                        android.util.Log.d("ChatDetail", "Adding message: isSent=$isSent, content=${msg.content}, imageUrl=${msg.imageUrl}")
                    }
                    
                    messageAdapter.notifyDataSetChanged()
                    if (messages.isNotEmpty()) {
                        rvMessages.scrollToPosition(messages.size - 1)
                    }
                    
                    android.util.Log.d("ChatDetail", "Loaded ${messages.size} messages in real-time")
                    
                    // Mark messages as read
                    lifecycleScope.launch {
                        chatRepository.markChatMessagesAsRead(chatId)
                    }
                }
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
        
        // Sử dụng sharedChatId, nếu null thì tạo từ receiverId
        val chatIdToUse = if (sharedChatId != null) {
            sharedChatId
        } else {
            val localReceiverId = receiverId
            if (localReceiverId != null) {
                // Generate sharedChatId từ currentUserId và receiverId
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    if (currentUserId < localReceiverId) {
                        "chat_${currentUserId}_${localReceiverId}"
                    } else {
                        "chat_${localReceiverId}_${currentUserId}"
                    }
                } else {
                    chatId
                }
            } else {
                chatId
            }
        }
        
        if (chatIdToUse == null) {
            Toast.makeText(this, "Lỗi: Không có chatId", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Clear input immediately for better UX
        etMessageInput.text.clear()
        
        // Send to Firebase (realtime listener will add to UI automatically)
        btnSend.isEnabled = false
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("ChatDetail", "Sending message to sharedChatId: $chatIdToUse, receiverId: $receiverId")
                
                val result = chatRepository.sendMessage(
                    chatId = chatIdToUse,
                    content = messageText,
                    receiverId = receiverId,
                    receiverName = receiverName
                )
                
                result.onSuccess { messageId ->
                    android.util.Log.d("ChatDetail", "Message sent successfully: $messageId")
                }
                
                result.onFailure { error ->
                    android.util.Log.e("ChatDetail", "Failed to send message", error)
                    Toast.makeText(
                        this@ChatDetailActivity,
                        "Lỗi gửi tin nhắn: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ChatDetail", "Exception sending message", e)
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
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
    
    private fun uploadAndSendImage(imageUri: Uri) {
        val chatIdToUse = if (sharedChatId != null) {
            sharedChatId
        } else {
            val localReceiverId = receiverId
            if (localReceiverId != null) {
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId != null) {
                    if (currentUserId < localReceiverId) {
                        "chat_${currentUserId}_${localReceiverId}"
                    } else {
                        "chat_${localReceiverId}_${currentUserId}"
                    }
                } else {
                    chatId
                }
            } else {
                chatId
            }
        }
        
        if (chatIdToUse == null) {
            Toast.makeText(this, "Lỗi: Không có chatId", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show progress
        progressBar.visibility = View.VISIBLE
        btnAddImage.isEnabled = false
        btnSend.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // Create unique filename
                val timestamp = System.currentTimeMillis()
                val filename = "chat_images/${chatIdToUse}/${timestamp}_${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child(filename)
                
                // Upload image
                val uploadTask = storageRef.putFile(imageUri).await()
                
                // Get download URL
                val downloadUrl = storageRef.downloadUrl.await()
                
                android.util.Log.d("ChatDetail", "Image uploaded: $downloadUrl")
                
                // Get message text if any
                val messageText = etMessageInput.text.toString().trim()
                etMessageInput.text.clear()
                
                // Send message with image URL
                val result = chatRepository.sendMessageWithImage(
                    chatId = chatIdToUse,
                    content = messageText,
                    imageUrl = downloadUrl.toString(),
                    receiverId = receiverId,
                    receiverName = receiverName
                )
                
                result.onSuccess { messageId ->
                    android.util.Log.d("ChatDetail", "Image message sent successfully: $messageId")
                    Toast.makeText(this@ChatDetailActivity, "Đã gửi ảnh", Toast.LENGTH_SHORT).show()
                }
                
                result.onFailure { error ->
                    android.util.Log.e("ChatDetail", "Failed to send image message", error)
                    Toast.makeText(
                        this@ChatDetailActivity,
                        "Lỗi gửi ảnh: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ChatDetail", "Exception uploading image", e)
                Toast.makeText(
                    this@ChatDetailActivity,
                    "Lỗi tải ảnh: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
                btnAddImage.isEnabled = true
                btnSend.isEnabled = true
                selectedImageUri = null
            }
        }
    }
}