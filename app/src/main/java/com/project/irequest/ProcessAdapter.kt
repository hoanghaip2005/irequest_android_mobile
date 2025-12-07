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

            // Cập nhật màu sắc dựa trên trạng thái
            val statusColor = when (process.status.lowercase()) {
                "hoàn thành" -> R.color.custom_green
                "đang chờ" -> R.color.custom_orange
                else -> R.color.text_gray
            }
            statusChip.setChipBackgroundColorResource(statusColor)

            // Xử lý sự kiện click để mở màn hình chi tiết
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ProcessDetailActivity::class.java).apply {
                    putExtra("EXTRA_PROCESS", process) // Truyền đối tượng Process
                }
                context.startActivity(intent)
            }
        }
    }
}
