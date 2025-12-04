package com.project.irequest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request

class RequestCardAdapter(
    private var requests: List<Request>,
    private val onRequestClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestCardAdapter.RequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_card, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
        holder.itemView.setOnClickListener {
            onRequestClick(request)
        }
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<Request>) {
        requests = newRequests
        notifyDataSetChanged()
    }

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRequestId: TextView = itemView.findViewById(R.id.tvRequestId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvAssignee: TextView = itemView.findViewById(R.id.tvAssignee)

        fun bind(request: Request) {
            tvRequestId.text = "#${request.requestId}"
            tvTitle.text = request.title
            tvDescription.text = request.description ?: "Không có mô tả"
            
            // Set status
            tvStatus.text = request.statusName ?: "Chưa xử lý"
            tvStatus.setBackgroundColor(getStatusColor(request.statusId))
            
            // Set priority
            tvPriority.text = request.priorityName ?: "Thường"
            tvPriority.setTextColor(getPriorityColor(request.priorityId))
            
            // Set assignee
            tvAssignee.text = request.assignedUserName ?: "Chưa giao"
            
            // Hide description if empty
            if (request.description.isNullOrEmpty()) {
                tvDescription.visibility = View.GONE
            } else {
                tvDescription.visibility = View.VISIBLE
            }
        }

        private fun getStatusColor(statusId: Int?): Int {
            return when (statusId) {
                1 -> Color.parseColor("#FF9800") // Pending - Orange
                2 -> Color.parseColor("#2196F3") // In Progress - Blue
                3 -> Color.parseColor("#4CAF50") // Completed - Green
                4 -> Color.parseColor("#F44336") // Rejected - Red
                else -> Color.parseColor("#9E9E9E") // Unknown - Gray
            }
        }

        private fun getPriorityColor(priorityId: Int?): Int {
            return when (priorityId) {
                1 -> Color.parseColor("#4CAF50") // Low - Green
                2 -> Color.parseColor("#FF9800") // Medium - Orange
                3 -> Color.parseColor("#F44336") // High - Red
                4 -> Color.parseColor("#9C27B0") // Urgent - Purple
                else -> Color.parseColor("#666666") // Unknown - Gray
            }
        }
    }
}
