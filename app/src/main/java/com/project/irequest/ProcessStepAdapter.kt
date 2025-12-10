package com.project.irequest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProcessStepAdapter(
    private val steps: List<ProcessStep>,
    private val onStepClick: (ProcessStep) -> Unit = {}
) : RecyclerView.Adapter<ProcessStepAdapter.StepViewHolder>() {

    class StepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepNumber: TextView = view.findViewById(R.id.tvStepNumber)
        val status: TextView = view.findViewById(R.id.tvStatus)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val date: TextView = view.findViewById(R.id.tvDate)
        val assignee: TextView = view.findViewById(R.id.tvAssignee)
        val timelineDot: View = view.findViewById(R.id.timeline_dot)
        val timelineLine: View = view.findViewById(R.id.timeline_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_process_step_timeline, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[position]
        
        // Click event
        holder.itemView.setOnClickListener {
            onStepClick(step)
        }
        
        // Step number
        holder.stepNumber.text = "Bước ${position + 1}"
        
        // Title & Description
        holder.title.text = step.title
        holder.description.text = step.description
        holder.date.text = step.date
        
        // Status badge với màu sắc
        when(step.status) {
            StepStatus.COMPLETED -> {
                holder.status.text = "✓ Hoàn thành"
                holder.status.setBackgroundResource(R.drawable.bg_pill_green)
                holder.timelineDot.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            StepStatus.CURRENT -> {
                holder.status.text = "● Đang xử lý"
                holder.status.setBackgroundResource(R.drawable.bg_pill_orange)
                holder.timelineDot.setBackgroundColor(Color.parseColor("#FF9800"))
                holder.title.setTextColor(Color.parseColor("#FF9800"))
            }
            StepStatus.PENDING -> {
                holder.status.text = "○ Chờ xử lý"
                holder.status.setBackgroundColor(Color.parseColor("#9E9E9E"))
                holder.timelineDot.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }
            StepStatus.UPCOMING -> {
                holder.status.text = "○ Sắp tới"
                holder.status.setBackgroundColor(Color.parseColor("#E0E0E0"))
                holder.status.setTextColor(Color.parseColor("#666666"))
                holder.timelineDot.setBackgroundColor(Color.parseColor("#E0E0E0"))
            }
        }
        
        // Hide timeline line for last item
        if (position == steps.size - 1) {
            holder.timelineLine.visibility = View.INVISIBLE
        } else {
            holder.timelineLine.visibility = View.VISIBLE
        }
        
        // TODO: Show assignee if available
        // holder.assignee.text = step.assignedUser
        // holder.assignee.visibility = View.VISIBLE
    }

    override fun getItemCount() = steps.size
}
