package com.irequest.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.irequest.databinding.TabCommentsBinding
import com.project.irequest.databinding.ItemCommentBinding

class CommentsTabFragment : Fragment() {

    private var _binding: TabCommentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupAddComment()
    }

    private fun setupRecyclerView() {
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        val mockComments = listOf(
            CommentItem("Người xử lý", "Sẽ xử lý ngay", "2025-12-03 14:30"),
            CommentItem("Nhân viên hỗ trợ", "Đã liên hệ kỹ thuật viên", "2025-12-03 10:15")
        )
        binding.rvComments.adapter = CommentAdapter(mockComments)
    }

    private fun setupAddComment() {
        binding.btnSend.setOnClickListener {
            val text = binding.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                binding.etComment.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class CommentItem(val author: String, val text: String, val time: String)
}

class CommentAdapter(private val comments: List<CommentsTabFragment.CommentItem>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentsTabFragment.CommentItem) {
            binding.tvAuthor.text = comment.author
            binding.tvComment.text = comment.text
            binding.tvTimestamp.text = comment.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount() = comments.size
}
