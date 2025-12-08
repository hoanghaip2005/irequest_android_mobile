package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.irequest.data.models.Request
import com.example.irequest.data.models.request.CreateRequestModel
import com.example.irequest.data.repository.FirebaseRequestRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.*

class CreateRequestActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerDepartment: Spinner
    private lateinit var btnAttachment: MaterialButton
    private lateinit var tvAttachmentName: TextView
    private lateinit var btnRemoveAttachment: ImageButton
    private lateinit var layoutAttachment: LinearLayout
    private lateinit var btnSubmit: MaterialButton
    private lateinit var progressBar: ProgressBar
    
    private val requestRepository = FirebaseRequestRepository()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var selectedFileSize: Long? = null
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleFileSelection(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_request)
        
        initViews()
        setupToolbar()
        setupSpinners()
        setupListeners()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        btnAttachment = findViewById(R.id.btnAttachment)
        tvAttachmentName = findViewById(R.id.tvAttachmentName)
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment)
        layoutAttachment = findViewById(R.id.layoutAttachment)
        btnSubmit = findViewById(R.id.btnSubmit)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Tạo yêu cầu mới"
        }
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupSpinners() {
        // Priority spinner
        val priorities = arrayOf("Thấp", "Trung bình", "Cao", "Khẩn cấp")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
        spinnerPriority.setSelection(1) // Default: Trung bình
        
        // Department spinner
        val departments = arrayOf("IT", "Hành chính", "Nhân sự", "Kế toán", "Marketing", "Khác")
        val departmentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDepartment.adapter = departmentAdapter
    }
    
    private fun setupListeners() {
        btnAttachment.setOnClickListener {
            openFilePicker()
        }
        
        btnRemoveAttachment.setOnClickListener {
            removeAttachment()
        }
        
        btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun handleFileSelection(uri: Uri) {
        selectedFileUri = uri
        
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            
            selectedFileName = cursor.getString(nameIndex)
            selectedFileSize = cursor.getLong(sizeIndex)
            
            tvAttachmentName.text = selectedFileName
            layoutAttachment.visibility = View.VISIBLE
        }
    }
    
    private fun removeAttachment() {
        selectedFileUri = null
        selectedFileName = null
        selectedFileSize = null
        layoutAttachment.visibility = View.GONE
    }
    
    private fun validateAndSubmit() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        
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
        
        createRequest(title, description)
    }
    
    private fun createRequest(title: String, description: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }
        
        showLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Upload file if selected
                var attachmentUrl: String? = null
                var fileType: String? = null
                
                selectedFileUri?.let { uri ->
                    attachmentUrl = uploadFile(uri)
                    fileType = contentResolver.getType(uri)
                }
                
                // Create request object
                val priorityId = spinnerPriority.selectedItemPosition + 1
                val priorityName = spinnerPriority.selectedItem.toString()
                
                val createRequest = CreateRequestModel(
                    title = title,
                    description = description,
                    priorityId = priorityId,
                    attachmentUrl = attachmentUrl,
                    attachmentFileName = selectedFileName,
                    attachmentFileType = fileType,
                    attachmentFileSize = selectedFileSize
                )
                
                // Save to Firestore
                val result = withContext(Dispatchers.IO) {
                    requestRepository.createRequest(createRequest)
                }
                
                result.onSuccess {
                    Toast.makeText(
                        this@CreateRequestActivity,
                        "Tạo yêu cầu thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(
                        this@CreateRequestActivity,
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CreateRequestActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private suspend fun uploadFile(uri: Uri): String = withContext(Dispatchers.IO) {
        val fileName = "${UUID.randomUUID()}_${selectedFileName}"
        val storageRef = storage.reference.child("attachments/$fileName")
        
        val uploadTask = storageRef.putFile(uri).await()
        storageRef.downloadUrl.await().toString()
    }
    
    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !show
        btnAttachment.isEnabled = !show
        etTitle.isEnabled = !show
        etDescription.isEnabled = !show
        spinnerPriority.isEnabled = !show
        spinnerDepartment.isEnabled = !show
    }
    
    companion object {
        const val REQUEST_CODE_CREATE = 1001
    }
}
