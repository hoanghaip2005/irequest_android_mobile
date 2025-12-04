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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.irequest.ui.data.RequestStore
import com.project.irequest.R

/**
 * RequestDetailFragment - Chi tiết một yêu cầu
 */
class RequestDetailFragment : Fragment() {

    private var requestId: String = ""
    private var isCommentsExpanded = false
    private var isAttachmentsExpanded = false
    private val attachmentsList = mutableListOf<String>() // Store attachment file paths

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestId = arguments?.getString("requestId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_request_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requestId.isNotEmpty()) {
            loadRequestDetails(view)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy yêu cầu", Toast.LENGTH_SHORT).show()
        }

        setupListeners(view)
    }

    private fun loadRequestDetails(view: View) {
        val request = RequestStore.getRequestById(requestId)

        if (request != null) {
            view.findViewById<TextView>(R.id.tvTitle).text = request.title
            view.findViewById<TextView>(R.id.tvDescription).text = request.description
            view.findViewById<TextView>(R.id.tvRequestId).text = "#REQ-${requestId.padStart(3, '0')}"
            view.findViewById<TextView>(R.id.tvCategory).text = request.category
            view.findViewById<TextView>(R.id.tvPriority).text = request.priority
            view.findViewById<TextView>(R.id.tvDeadline).text = request.deadline
            
            val statusChip = view.findViewById<Chip>(R.id.chipStatus)
            statusChip.text = when (request.status) {
                "NEW" -> "Mới"
                "IN_PROGRESS" -> "Đang xử lý"
                "DONE" -> "Hoàn thành"
                else -> request.status
            }
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy yêu cầu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        // Comments collapsible section
        val btnCommentsHeader = view.findViewById<LinearLayout>(R.id.btnCommentsHeader)
        val llCommentsContent = view.findViewById<LinearLayout>(R.id.llCommentsContent)
        val icCommentsExpand = view.findViewById<ImageView>(R.id.icCommentsExpand)
        
        btnCommentsHeader.setOnClickListener {
            isCommentsExpanded = !isCommentsExpanded
            llCommentsContent.visibility = if (isCommentsExpanded) View.VISIBLE else View.GONE
            icCommentsExpand.rotation = if (isCommentsExpanded) 180f else 0f
        }

        // Comment send button
        val etCommentInput = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCommentInput)
        val btnSendComment = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSendComment)
        val tvNoComments = view.findViewById<TextView>(R.id.tvNoComments)
        
        btnSendComment.setOnClickListener {
            val commentText = etCommentInput.text.toString().trim()
            if (commentText.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập bình luận", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // For now, just show success message
            Toast.makeText(requireContext(), "Bình luận đã được gửi", Toast.LENGTH_SHORT).show()
            etCommentInput.text?.clear()
            tvNoComments.visibility = View.GONE
            
            // TODO: Save comment to database/backend
        }

        // Attachments collapsible section
        val btnAttachmentsHeader = view.findViewById<LinearLayout>(R.id.btnAttachmentsHeader)
        val llAttachmentsContent = view.findViewById<LinearLayout>(R.id.llAttachmentsContent)
        val icAttachmentsExpand = view.findViewById<ImageView>(R.id.icAttachmentsExpand)
        val btnAddAttachment = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAddAttachment)
        val tvNoAttachments = view.findViewById<TextView>(R.id.tvNoAttachments)
        val rvAttachments = view.findViewById<RecyclerView>(R.id.rvAttachments)
        
        // Setup attachments RecyclerView
        rvAttachments.layoutManager = LinearLayoutManager(requireContext())
        
        btnAttachmentsHeader.setOnClickListener {
            isAttachmentsExpanded = !isAttachmentsExpanded
            llAttachmentsContent.visibility = if (isAttachmentsExpanded) View.VISIBLE else View.GONE
            icAttachmentsExpand.rotation = if (isAttachmentsExpanded) 180f else 0f
        }

        btnAddAttachment.setOnClickListener {
            requestFilePermission()
        }

        val btnEdit = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEdit)
        btnEdit?.setOnClickListener {
            Toast.makeText(requireContext(), "TODO: Edit request", Toast.LENGTH_SHORT).show()
        }

        val btnAssign = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAssign)
        btnAssign?.setOnClickListener {
            Toast.makeText(requireContext(), "TODO: Assign request", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestFilePermission() {
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
        
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = uri.lastPathSegment ?: "Tệp không xác định"
                attachmentsList.add(fileName)
                Toast.makeText(requireContext(), "Tệp được thêm: $fileName", Toast.LENGTH_SHORT).show()
                updateAttachmentsView()
            }
        }
    }

    private fun updateAttachmentsView() {
        val view = view ?: return
        val tvNoAttachments = view.findViewById<TextView>(R.id.tvNoAttachments)
        val rvAttachments = view.findViewById<RecyclerView>(R.id.rvAttachments)

        if (attachmentsList.isEmpty()) {
            tvNoAttachments.visibility = View.VISIBLE
            rvAttachments.visibility = View.GONE
        } else {
            tvNoAttachments.visibility = View.GONE
            rvAttachments.visibility = View.VISIBLE
            // Setup adapter for attachments
            val adapter = AttachmentAdapter(attachmentsList)
            rvAttachments.adapter = adapter
        }
    }

    // Simple Adapter for attachments
    private inner class AttachmentAdapter(private val items: List<String>) :
        RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(fileName: String) {
                itemView.findViewById<TextView>(R.id.tvFileName).text = fileName
                itemView.findViewById<ImageView>(R.id.btnDelete).setOnClickListener {
                    attachmentsList.remove(fileName)
                    notifyDataSetChanged()
                    updateAttachmentsView()
                }
                itemView.findViewById<ImageView>(R.id.btnOpen).setOnClickListener {
                    Toast.makeText(requireContext(), "Mở: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_attachment, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount() = items.size
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 1001
    }
}