package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.project.irequest.data.models.Request
import com.project.irequest.data.models.RequestPriority
import com.project.irequest.data.models.RequestStatus
import com.project.irequest.data.repository.RequestRepository
import kotlinx.coroutines.launch

class CreateRequestActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnAttachFile: Button
    private lateinit var tvAttachmentName: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnSubmit: Button

    private lateinit var requestRepository: RequestRepository
    private lateinit var auth: FirebaseAuth
    
    private var selectedFileUri: Uri? = null
    private val PICK_FILE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_request)

        // Initialize
        auth = FirebaseAuth.getInstance()
        requestRepository = RequestRepository()

        initViews()
        setupToolbar()
        setupPrioritySpinner()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        btnAttachFile = findViewById(R.id.btnAttachFile)
        tvAttachmentName = findViewById(R.id.tvAttachmentName)
        btnCancel = findViewById(R.id.btnCancel)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupPrioritySpinner() {
        val priorities = listOf(
            "Thấp",
            "Trung bình",
            "Cao",
            "Khẩn cấp"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
        
        // Set default to Medium priority
        spinnerPriority.setSelection(1)
    }

    private fun setupListeners() {
        btnAttachFile.setOnClickListener {
            openFilePicker()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            submitRequest()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            Intent.createChooser(intent, "Chọn tệp"),
            PICK_FILE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                val fileName = getFileName(uri)
                tvAttachmentName.text = fileName
                tvAttachmentName.setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun submitRequest() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val priorityPosition = spinnerPriority.selectedItemPosition

        // Validation
        if (title.isEmpty()) {
            etTitle.error = "Vui lòng nhập tiêu đề"
            etTitle.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etDescription.error = "Vui lòng nhập mô tả"
            etDescription.requestFocus()
            return
        }

        // Get current user ID
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        // Map priority
        val priorityId = when (priorityPosition) {
            0 -> RequestPriority.LOW
            1 -> RequestPriority.MEDIUM
            2 -> RequestPriority.HIGH
            3 -> RequestPriority.URGENT
            else -> RequestPriority.MEDIUM
        }

        // Disable submit button during processing
        btnSubmit.isEnabled = false
        btnSubmit.text = "Đang gửi..."

        // Create request object
        val newRequest = Request(
            title = title,
            description = description,
            priorityId = priorityId,
            statusId = RequestStatus.NEW,
            userId = userId,
            attachmentUrl = selectedFileUri?.toString() // TODO: Upload to Firebase Storage
        )

        // Create request
        lifecycleScope.launch {
            requestRepository.createRequest(newRequest).onSuccess {
                runOnUiThread {
                    Toast.makeText(
                        this@CreateRequestActivity,
                        "Tạo yêu cầu thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }.onFailure { error ->
                runOnUiThread {
                    Toast.makeText(
                        this@CreateRequestActivity,
                        "Lỗi: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Gửi yêu cầu"
                }
            }
        }
    }
}
