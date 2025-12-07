package com.project.irequest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter này để sửa lỗi "Unresolved reference '''ProcessStepAdapter'''"
class ProcessStepAdapter(private val steps: List<ProcessStep>) :
    RecyclerView.Adapter<ProcessStepAdapter.StepViewHolder>() {

    class StepViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Chúng ta dùng layout có sẵn của Android (simple_list_item_2) cho nhanh
        val title: TextView = view.findViewById(android.R.id.text1)
        val description: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return StepViewHolder(view)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val step = steps[position]
        
        holder.title.text = step.title
        
        // Hiển thị mô tả kèm trạng thái
        val statusText = when(step.status) {
            StepStatus.COMPLETED -> "[Đã xong]"
            StepStatus.CURRENT -> "[Đang xử lý]"
            else -> "[Chờ]"
        }
        holder.description.text = "$statusText ${step.description} \n${step.date}"
        
        // Đổi màu chữ chút cho dễ nhìn
        if (step.status == StepStatus.CURRENT) {
            holder.title.setTextColor(Color.BLUE)
        } else {
            holder.title.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount() = steps.size
}
