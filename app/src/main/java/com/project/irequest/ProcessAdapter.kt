package com.project.irequest

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class ProcessAdapter(private val processList: List<Process>) :
    RecyclerView.Adapter<ProcessAdapter.ProcessViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_process, parent, false)
        return ProcessViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProcessViewHolder, position: Int) {
        val process = processList[position]
        holder.bind(process)
    }

    override fun getItemCount(): Int = processList.size

    inner class ProcessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_process_name)
        private val dateTextView: TextView = itemView.findViewById(R.id.tv_process_date)
        private val statusChip: Chip = itemView.findViewById(R.id.chip_process_status)

        fun bind(process: Process) {
            nameTextView.text = process.name
            dateTextView.text = process.date
            statusChip.text = process.status

            // Cập nhật màu sắc dựa trên trạng thái (ĐÃ SỬA LỖI)
            val statusColor = when (process.status.lowercase()) {
                "hoàn thành" -> R.color.primary_blue // Sử dụng màu đã có
                "đang chờ" -> R.color.custom_orange   // Sử dụng màu đã có
                else -> R.color.text_gray
            }
            statusChip.setChipBackgroundColorResource(statusColor)

            // Xử lý sự kiện click để mở màn hình chi tiết bước quy trình
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ProcessStepManagementActivity::class.java).apply {
                    putExtra("PROCESS_ID", position) // Truyền ID/position của quy trình
                    putExtra("PROCESS_NAME", process.name) // Truyền tên quy trình
                    putExtra("PROCESS_STATUS", process.status) // Truyền trạng thái
                }
                context.startActivity(intent)
            }
        }
    }
}
