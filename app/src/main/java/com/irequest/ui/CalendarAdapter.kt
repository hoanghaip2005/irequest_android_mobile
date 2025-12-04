package com.project.irequest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

data class CalendarDay(
    val date: Date?,
    val dayOfMonth: Int,
    val hasRequests: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)

class CalendarAdapter(
    private var days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            if (day.date != null && day.isCurrentMonth) {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onDayClick(day)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        selectedPosition = -1
        notifyDataSetChanged()
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDay: TextView = itemView.findViewById(R.id.tvDay)
        private val dotIndicator: View = itemView.findViewById(R.id.dotIndicator)
        private val dayContainer: FrameLayout = itemView.findViewById(R.id.dayContainer)

        fun bind(day: CalendarDay, isSelected: Boolean) {
            tvDay.text = if (day.dayOfMonth > 0) day.dayOfMonth.toString() else ""
            
            // Set text color based on state
            when {
                !day.isCurrentMonth -> {
                    tvDay.setTextColor(Color.parseColor("#CCCCCC"))
                    dayContainer.isEnabled = false
                }
                isSelected -> {
                    tvDay.setTextColor(Color.WHITE)
                }
                day.isToday -> {
                    tvDay.setTextColor(Color.parseColor("#2196F3"))
                }
                else -> {
                    tvDay.setTextColor(Color.parseColor("#333333"))
                    dayContainer.isEnabled = true
                }
            }
            
            // Show dot indicator if has requests
            val shouldShowDot = day.hasRequests && day.isCurrentMonth
            dotIndicator.visibility = if (shouldShowDot) View.VISIBLE else View.GONE
            
            // Debug log for days with requests
            if (day.hasRequests && day.isCurrentMonth) {
                android.util.Log.d("CalendarAdapter", "Day ${day.dayOfMonth} has requests - showing dot")
            }
            
            // Set selected state
            dayContainer.isSelected = isSelected
        }
    }
}
