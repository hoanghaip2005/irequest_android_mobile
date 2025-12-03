package com.irequest.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.irequest.databinding.ItemRequestBinding

class RequestAdapter(
    private val requests: List<RequestItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(request: RequestItem) {
            binding.apply {
                tvTitle.text = request.title
                tvDescription.text = request.description
                tvCreatedDate.text = request.date
                tvCategory.text = request.category
                tvPriority.text = request.priority
                tvAssignee.text = request.assignee
                tvDeadline.text = request.deadline
                
                // Status chip
                chipStatus.text = when (request.status) {
                    "NEW" -> "Mới"
                    "IN_PROGRESS" -> "Đang xử lý"
                    "DONE" -> "Hoàn thành"
                    else -> request.status
                }
                
                root.setOnClickListener {
                    onItemClick(request.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position])
    }

    override fun getItemCount() = requests.size

    data class RequestItem(
        val id: Int,
        val title: String,
        val date: String,
        val category: String = "IT",
        val priority: String = "NORMAL",
        val description: String = "",
        val status: String = "NEW",
        val assignee: String = "Chưa phân công",
        val deadline: String = ""
    )
}
