package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.irequest.databinding.TabAttachmentsBinding
import com.project.irequest.databinding.ItemAttachmentBinding

class AttachmentsTabFragment : Fragment() {

    private var _binding: TabAttachmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabAttachmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvAttachments.layoutManager = LinearLayoutManager(requireContext())
        val mockAttachments = listOf(
            AttachmentItem("Báo cáo kỹ thuật", "report.pdf", "2.5 MB"),
            AttachmentItem("Hình ảnh thiết bị", "equipment.jpg", "1.2 MB")
        )
        binding.rvAttachments.adapter = AttachmentAdapter(mockAttachments)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class AttachmentItem(val name: String, val fileName: String, val size: String)
}

class AttachmentAdapter(private val attachments: List<AttachmentsTabFragment.AttachmentItem>) :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {

    inner class AttachmentViewHolder(private val binding: ItemAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(attachment: AttachmentsTabFragment.AttachmentItem) {
            binding.tvFileName.text = attachment.fileName
            binding.tvFileSize.text = attachment.size
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        val binding = ItemAttachmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AttachmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(attachments[position])
    }

    override fun getItemCount() = attachments.size
}
