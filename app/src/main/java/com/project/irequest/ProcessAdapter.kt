package com.project.irequest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ProcessAdapter(private val processList: List<Process>) :
    RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_process, parent, false)
        return ProcessViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
        val process = processList[position]
        holder.tvProcessName.text = process.name
        holder.tvProcessDate.text = "Ngày tạo: ${process.creationDate}"
        holder.tvProcessStatus.text = process.status

        // Cập nhật màu sắc của trạng thái
        if (process.status.equals("Hoàn thành", ignoreCase = true)) {
            holder.tvProcessStatus.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_badge_green)
        } else {
            holder.tvProcessStatus.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_badge_red)
        }

        // Xử lý sự kiện nhấp chuột
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProcessDetailActivity::class.java).apply {
                putExtra("EXTRA_PROCESS", process)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return processList.size
    }

    class ProcessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProcessName: TextView = itemView.findViewById(R.id.tv_process_name)
        val tvProcessDate: TextView = itemView.findViewById(R.id.tv_process_date)
        val tvProcessStatus: TextView = itemView.findViewById(R.id.tv_process_status)
    }
}