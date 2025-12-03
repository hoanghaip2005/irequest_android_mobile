package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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

        // Attachments collapsible section
        val btnAttachmentsHeader = view.findViewById<LinearLayout>(R.id.btnAttachmentsHeader)
        val llAttachmentsContent = view.findViewById<LinearLayout>(R.id.llAttachmentsContent)
        val icAttachmentsExpand = view.findViewById<ImageView>(R.id.icAttachmentsExpand)
        
        btnAttachmentsHeader.setOnClickListener {
            isAttachmentsExpanded = !isAttachmentsExpanded
            llAttachmentsContent.visibility = if (isAttachmentsExpanded) View.VISIBLE else View.GONE
            icAttachmentsExpand.rotation = if (isAttachmentsExpanded) 180f else 0f
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

    companion object {
        fun newInstance(requestId: String): RequestDetailFragment {
            return RequestDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("requestId", requestId)
                }
            }
        }
    }
}