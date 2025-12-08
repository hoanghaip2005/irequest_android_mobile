package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Request
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: List<Request>,
    private val onItemClick: (Request) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView as MaterialCardView
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvCreatedBy: TextView = itemView.findViewById(R.id.tvCreatedBy)
        val tvCreatedDate: TextView = itemView.findViewById(R.id.tvCreatedDate)
        val chipStatus: Chip = itemView.findViewById(R.id.chipStatus)
        val chipPriority: Chip = itemView.findViewById(R.id.chipPriority)
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
        
        holder.cardView.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size
}
