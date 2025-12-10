package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.RequestActivity
import java.text.SimpleDateFormat
import java.util.*

class RequestActivityAdapter(
    private var activities: List<RequestActivity> = emptyList()
) : RecyclerView.Adapter<RequestActivityAdapter.ActivityViewHolder>() {

    private val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))
    
    fun updateData(newActivities: List<RequestActivity>) {
        activities = newActivities
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(activities[position])
    }

    override fun getItemCount(): Int = activities.size

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(activity: RequestActivity) {
            tvDate.text = dateFormat.format(activity.createdAt).uppercase()
            tvTitle.text = "Yêu cầu: ${activity.requestTitle}"
            
            // Set status badge
            tvStatus.text = activity.action
            when (activity.action) {
                "Tạo mới" -> {
                    tvStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_blue_light))
                }
                "Phê duyệt", "Hoàn thành" -> {
                    tvStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                "Từ chối" -> {
                    tvStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_red_light))
                }
                "Đang xử lý", "Cập nhật" -> {
                    tvStatus.setBackgroundColor(itemView.context.getColor(android.R.color.holo_orange_light))
                }
                else -> {
                    tvStatus.setBackgroundColor(itemView.context.getColor(android.R.color.darker_gray))
                }
            }
        }
    }
}
