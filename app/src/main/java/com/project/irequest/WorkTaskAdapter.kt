package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class WorkTaskAdapter(
    private val tasks: List<Request>,
    private val onItemClick: (Request) -> Unit,
    private val onApproveClick: (Request) -> Unit,
    private val onRejectClick: (Request) -> Unit,
    private val onCompleteClick: (Request) -> Unit,
    private val canProcess: (Request) -> Boolean
) : RecyclerView.Adapter<WorkTaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvCreatedBy: TextView = itemView.findViewById(R.id.tvCreatedBy)
        val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val chipPriority: Chip = itemView.findViewById(R.id.chipPriority)
        val llActionButtons: LinearLayout = itemView.findViewById(R.id.llActionButtons)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
        val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.tvTitle.text = task.title
        holder.tvDescription.text = task.description ?: "Không có mô tả"
        holder.tvCreatedBy.text = "Từ: ${task.userName ?: "Không rõ"}"
        
        task.createdAt?.let {
            holder.tvCreatedDate.text = dateFormat.format(it)
        } ?: run {
            holder.tvCreatedDate.text = "N/A"
        }
        
        // Status
        holder.chipStatus.text = task.statusName ?: "Mới"
        val (statusBgColor, statusStrokeColor) = when (task.statusId) {
            1 -> Pair("#E3F2FD", "#2196F3") // New
            2 -> Pair("#FFF3E0", "#FF9800") // In Progress
            3 -> Pair("#E8F5E9", "#4CAF50") // Completed
            4 -> Pair("#F5F5F5", "#9E9E9E") // Closed
            else -> Pair("#F5F5F5", "#000000")
        }
        holder.chipStatus.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(statusBgColor)
        )
        holder.chipStatus.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(statusStrokeColor)
        )
        
        // Priority
        holder.chipPriority.text = task.priorityName ?: "Thường"
        val (priorityBgColor, priorityStrokeColor) = when (task.priorityId) {
            1 -> Pair("#E8F5E9", "#4CAF50") // Low
            2 -> Pair("#E3F2FD", "#2196F3") // Medium
            3 -> Pair("#FFF3E0", "#FF9800") // High
            4 -> Pair("#FFEBEE", "#F44336") // Urgent
            else -> Pair("#F5F5F5", "#9E9E9E")
        }
        holder.chipPriority.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityBgColor)
        )
        holder.chipPriority.chipStrokeColor = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(priorityStrokeColor)
        )
        
        // Action buttons
        val hasPermission = canProcess(task)
        if (hasPermission) {
            holder.llActionButtons.visibility = View.VISIBLE
            
            // Hiển thị nút phù hợp với status
            when (task.statusId) {
                1 -> { // New - Hiển thị Phê duyệt và Từ chối
                    holder.btnApprove.visibility = View.VISIBLE
                    holder.btnReject.visibility = View.VISIBLE
                    holder.btnComplete.visibility = View.GONE
                }
                2 -> { // In Progress - Hiển thị Hoàn thành và Từ chối
                    holder.btnApprove.visibility = View.GONE
                    holder.btnReject.visibility = View.VISIBLE
                    holder.btnComplete.visibility = View.VISIBLE
                }
                else -> { // Completed/Closed - Ẩn tất cả nút
                    holder.llActionButtons.visibility = View.GONE
                }
            }
        } else {
            holder.llActionButtons.visibility = View.GONE
        }
        
        // Click listeners
        holder.cardView.setOnClickListener {
            onItemClick(task)
        }
        
        holder.btnApprove.setOnClickListener {
            onApproveClick(task)
        }
        
        holder.btnReject.setOnClickListener {
            onRejectClick(task)
        }
        
        holder.btnComplete.setOnClickListener {
            onCompleteClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size
}
