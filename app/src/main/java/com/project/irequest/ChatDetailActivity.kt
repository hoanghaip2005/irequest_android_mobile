package com.project.irequest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.util.UUID

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<ChatMessage>()
    
    private lateinit var storageRef: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var ivGallery: ImageView
    private lateinit var etMessageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        // Initialize Firebase
        storageRef = Firebase.storage.reference
        auth = FirebaseAuth.getInstance()

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
        etMessageInput = findViewById(R.id.et_message_input)
        val btnSend: Button = findViewById(R.id.btn_send)
        ivGallery = findViewById(R.id.iv_gallery)

        // Gallery picker
        ivGallery.setOnClickListener {
            openGalleryPicker()
        }

        btnSend.setOnClickListener {
            sendTextMessage()
        }
    }
    
    private fun openGalleryPicker() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        imagePickerActivityResult.launch(galleryIntent)
    }
    
    private fun sendTextMessage() {
        val messageText = etMessageInput.text.toString()
        if (messageText.isNotEmpty()) {
            val newMessage = ChatMessage(messageText, true, isImage = false)
            messages.add(newMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            rvMessages.scrollToPosition(messages.size - 1)
            etMessageInput.text.clear()
        } else {
            Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
        }
    }
    
    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    uploadImageToFirebase(imageUri)
                }
            }
        }
    
    private fun uploadImageToFirebase(imageUri: Uri) {
        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để tải ảnh lên", Toast.LENGTH_SHORT).show()
            Log.e("ChatDetail", "User not authenticated")
            return
        }
        
        // Show loading
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
        
        // Extract file name
        val fileName = getFileName(applicationContext, imageUri) ?: "${UUID.randomUUID()}.jpg"
        
        // Create unique path with authenticated user ID
        val userId = currentUser.uid
        val imagePath = "chat_images/$userId/${System.currentTimeMillis()}_$fileName"
        
        Log.d("ChatDetail", "Uploading image for user: $userId, path: $imagePath")
        
        // Upload to Firebase Storage
        val uploadTask = storageRef.child(imagePath).putFile(imageUri)
        
        uploadTask.addOnSuccessListener {
            // Get download URL
            storageRef.child(imagePath).downloadUrl.addOnSuccessListener { downloadUri ->
                // Add image message
                val imageMessage = ChatMessage(
                    content = downloadUri.toString(),
                    isSentByMe = true,
                    isImage = true,
                    imagePath = imagePath,
                    fileName = fileName
                )
                messages.add(imageMessage)
                messageAdapter.notifyItemInserted(messages.size - 1)
                rvMessages.scrollToPosition(messages.size - 1)
                
                Toast.makeText(this, "Đã tải ảnh lên thành công", Toast.LENGTH_SHORT).show()
                Log.d("ChatDetail", "Image uploaded: $downloadUri")
                
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi lấy URL ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ChatDetail", "Failed to get download URL", e)
            }
            
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Lỗi tải ảnh lên: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("ChatDetail", "Image upload failed", e)
        }
    }
    
    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it + 1) }
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