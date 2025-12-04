package com.irequest.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.irequest.ui.data.RequestItem
import com.irequest.ui.data.RequestStore
import com.project.irequest.databinding.FragmentEditRequestBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EditRequestFragment : Fragment() {

    private var _binding: FragmentEditRequestBinding? = null
    private val binding get() = _binding!!
    
    private val attachmentsList = mutableListOf<String>()
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val requestId = arguments?.getString("requestId")
        
        if (!requestId.isNullOrEmpty()) {
            // Load existing data
            binding.etTitle.setText("Sửa chữa máy lạnh")
            binding.etDescription.setText("Máy lạnh phòng họp không hoạt động")
        }
        
        setupSpinners()
        setupToolbar()
        setupListeners()
        setupAttachmentsView()
    }

    private fun setupSpinners() {
        // Category Spinner
        val categories = arrayOf("CNTT", "Nhân sự", "Kế toán", "Marketing", "Khác")
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Priority Spinner
        val priorities = arrayOf("Thấp", "Trung bình", "Cao", "Rất cao")
        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorities
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSubmit.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()
            val priority = binding.spinnerPriority.selectedItem.toString()
            
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Create new request
            val newRequest = RequestItem(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                category = category,
                priority = priority,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                status = "NEW",
                assignee = "Chưa phân công",
                deadline = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                    Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
                )
            )
            
            // Add to store
            RequestStore.addRequest(newRequest)
            
            Toast.makeText(requireContext(), "Yêu cầu đã được lưu", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.btnAddAttachment.setOnClickListener {
            requestFilePermission()
        }
    }

    private fun requestFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - request READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openFilePicker()
            }
        } else {
            // Android 12 and below - request READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                openFilePicker()
            }
        }
    }

    private fun openFilePicker() {
        // Use Intent directly - more reliable than ActivityResultContracts
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        
        try {
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = uri.lastPathSegment ?: "Tệp không xác định"
                attachmentsList.add(fileName)
                Toast.makeText(requireContext(), "Tệp được thêm: $fileName", Toast.LENGTH_SHORT).show()
                updateAttachmentsView()
            }
        }
    }

    private fun setupAttachmentsView() {
        updateAttachmentsView()
    }

    private fun updateAttachmentsView() {
        val rvAttachments = binding.rvAttachments
        val llEmptyAttachments = binding.llEmptyAttachments

        if (attachmentsList.isEmpty()) {
            llEmptyAttachments.visibility = View.VISIBLE
            rvAttachments.visibility = View.GONE
        } else {
            llEmptyAttachments.visibility = View.GONE
            rvAttachments.visibility = View.VISIBLE
            // Setup adapter for attachments
            val adapter = AttachmentAdapter(attachmentsList)
            rvAttachments.layoutManager = LinearLayoutManager(requireContext())
            rvAttachments.adapter = adapter
        }
    }

    // Simple Adapter for attachments
    private inner class AttachmentAdapter(private val items: MutableList<String>) :
        RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(fileName: String) {
                itemView.findViewById<android.widget.TextView>(com.project.irequest.R.id.tvFileName).text = fileName
                itemView.findViewById<android.widget.ImageView>(com.project.irequest.R.id.btnDelete).setOnClickListener {
                    items.remove(fileName)
                    notifyDataSetChanged()
                    updateAttachmentsView()
                }
                itemView.findViewById<android.widget.ImageView>(com.project.irequest.R.id.btnOpen).setOnClickListener {
                    Toast.makeText(requireContext(), "Mở: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(com.project.irequest.R.layout.item_attachment, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Quyền đã được cấp", Toast.LENGTH_SHORT).show()
                openFilePicker()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Cần cấp quyền để chọn tệp",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
