package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class RequestAdapter(
    private val requests: List<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val tvAssignee: TextView = itemView.findViewById(R.id.tvAssignee)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        
        holder.tvTitle.text = request.title
        holder.tvDescription.text = request.description ?: "Không có mô tả"
        holder.chipStatus.text = request.statusName ?: "Mới"
        holder.tvPriority.text = request.priorityName ?: "Thường"
        holder.tvCategory.text = "Request #${request.requestId}"
        
        request.createdAt?.let {
            holder.tvCreatedDate.text = dateFormat.format(it)
        } ?: run {
            holder.tvCreatedDate.text = "N/A"
        }
        
        holder.tvAssignee.text = request.assignedUserName ?: "Chưa phân công"
        
        // Set priority color
        val priorityColor = when (request.priorityId) {
            1 -> android.graphics.Color.parseColor("#4CAF50") // Low - Green
            2 -> android.graphics.Color.parseColor("#2196F3") // Medium - Blue
            3 -> android.graphics.Color.parseColor("#FF9800") // High - Orange
            4 -> android.graphics.Color.parseColor("#F44336") // Urgent - Red
            else -> android.graphics.Color.parseColor("#9E9E9E") // Default - Gray
        }
        holder.tvPriority.setTextColor(priorityColor)
        
        // Set status chip colors
        val (bgColor, strokeColor) = when (request.statusId) {
            1 -> Pair("#E3F2FD", "#2196F3") // New - Blue
            2 -> Pair("#FFF3E0", "#FF9800") // In Progress - Orange
            3 -> Pair("#E8F5E9", "#4CAF50") // Completed - Green
            4 -> Pair("#F5F5F5", "#9E9E9E") // Closed - Gray
            else -> Pair("#F5F5F5", "#000000") // Default
        }
        holder.chipStatus.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(bgColor)
        )
        holder.chipStatus.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(strokeColor)
        )
        
        holder.cardView.setOnClickListener {
            onItemClick(request)
        }
    }

    override fun getItemCount(): Int = requests.size
}
