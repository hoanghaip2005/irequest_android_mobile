package com.project.irequest

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AlertsAdapter(
    private var alerts: MutableList<AlertData>,
    private val onClick: (AlertData) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    fun updateData(newAlerts: List<AlertData>) {
        alerts.clear()
        alerts.addAll(newAlerts)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in alerts.indices) {
            alerts.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // 👇 MỚI: Hàm này để chèn lại item khi bấm Hoàn tác
    fun insertItem(position: Int, item: AlertData) {
        alerts.add(position, item)
        notifyItemInserted(position)
    }

    fun getItem(position: Int): AlertData = alerts[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount(): Int = alerts.size

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val flIconBg: FrameLayout = itemView.findViewById(R.id.flIconBg)
        private val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)

        fun bind(data: AlertData) {
            tvTitle.text = data.title
            tvMessage.text = data.message

            if (data.badgeText != null) {
                tvBadge.text = data.badgeText
                tvBadge.visibility = View.VISIBLE
                tvTime.visibility = View.GONE
            } else {
                tvBadge.visibility = View.GONE
                tvTime.text = data.time
                tvTime.visibility = View.VISIBLE
            }

            // Logic Đã đọc / Chưa đọc
            if (data.isRead) {
                tvTitle.typeface = Typeface.DEFAULT
                viewUnreadDot.visibility = View.GONE
                cardView.cardElevation = 0f
                cardView.setCardBackgroundColor(Color.parseColor("#80FFFFFF"))
            } else {
                tvTitle.typeface = Typeface.DEFAULT_BOLD
                viewUnreadDot.visibility = View.VISIBLE
                cardView.cardElevation = 4f
                cardView.setCardBackgroundColor(Color.WHITE)
            }

            val context = itemView.context
            val (iconRes, colorRes) = when (data.type) {
                AlertType.REQUEST_UPDATE -> Pair(android.R.drawable.ic_dialog_info, R.color.primary_blue)
                AlertType.REQUEST_APPROVED -> Pair(android.R.drawable.checkbox_on_background, R.color.custom_green)
                AlertType.SLA_WARNING -> Pair(android.R.drawable.ic_dialog_alert, R.color.custom_orange)
                AlertType.CHAT_MESSAGE -> Pair(android.R.drawable.ic_dialog_email, R.color.primary_blue)
                else -> Pair(android.R.drawable.ic_popup_reminder, R.color.text_gray)
            }

            ivIcon.setImageResource(iconRes)
            // Lấy màu an toàn
            val color = try {
                ContextCompat.getColor(context, colorRes)
            } catch (e: Exception) {
                Color.GRAY
            }
            ivIcon.setColorFilter(color)

            val bgShape = GradientDrawable()
            bgShape.shape = GradientDrawable.OVAL
            bgShape.setColor(Color.parseColor("#1A" + Integer.toHexString(color).substring(2)))
            flIconBg.background = bgShape

            itemView.setOnClickListener { onClick(data) }
        }
    }
}