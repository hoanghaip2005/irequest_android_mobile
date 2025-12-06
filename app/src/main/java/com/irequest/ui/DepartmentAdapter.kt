package com.project.irequest.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Department
import com.project.irequest.R
import java.util.Locale
import kotlin.math.abs

class DepartmentAdapter(
    private var originalList: List<Department> // List gốc để giữ dữ liệu
) : RecyclerView.Adapter<DepartmentAdapter.DeptViewHolder>() {

    // List đang hiển thị (sẽ thay đổi khi tìm kiếm)
    private var displayedList: MutableList<Department> = originalList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_department, parent, false)
        return DeptViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeptViewHolder, position: Int) {
        holder.bind(displayedList[position])
    }

    override fun getItemCount(): Int = displayedList.size

    // Cập nhật dữ liệu mới từ Activity
    fun updateData(newData: List<Department>) {
        originalList = newData
        displayedList = newData.toMutableList()
        notifyDataSetChanged()
    }

    // 👇 LOGIC TÌM KIẾM ĐƯỢC CẢI TIẾN
    fun filter(query: String) {
        if (query.isEmpty()) {
            // Nếu xóa hết chữ tìm kiếm -> Hiện lại toàn bộ
            displayedList = originalList.toMutableList()
            // Đóng tất cả lại cho gọn
            displayedList.forEach { it.isExpanded = false }
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            val filtered = mutableListOf<Department>()

            for (dept in originalList) {
                // 1. Tìm theo tên phòng (Ví dụ gõ "IT" -> ra phòng IT)
                val matchDeptName = dept.name.lowercase().contains(lowerQuery)

                // 2. Tìm theo tên nhân viên (Ví dụ gõ "Code" -> ra phòng IT vì có Lê Văn Code)
                val matchEmployee = dept.employees.any {
                    it.name.lowercase().contains(lowerQuery)
                }

                if (matchDeptName || matchEmployee) {
                    // Nếu tìm thấy nhân viên bên trong -> Tự động mở rộng phòng đó ra
                    dept.isExpanded = matchEmployee // Chỉ mở nếu khớp tên nhân viên
                    filtered.add(dept)
                }
            }
            displayedList = filtered
        }
        notifyDataSetChanged()
    }

    inner class DeptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeptName: TextView = itemView.findViewById(R.id.tvDeptName)
        private val tvManagerName: TextView = itemView.findViewById(R.id.tvManagerName)
        private val ivDeptIcon: ImageView = itemView.findViewById(R.id.ivDeptIcon)
        private val ivArrow: ImageView = itemView.findViewById(R.id.ivArrow)
        private val layoutExpand: LinearLayout = itemView.findViewById(R.id.layoutExpand)
        private val layoutHeader: LinearLayout = itemView.findViewById(R.id.layoutHeader)

        private val avatarColors = listOf(
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#009688",
            "#4CAF50", "#8BC34A", "#FFC107", "#FF9800"
        )

        fun bind(dept: Department) {
            tvDeptName.text = dept.name
            val prefix = if (dept.name.contains("Giám Đốc", true)) "Lãnh đạo:" else "Trưởng phòng:"
            tvManagerName.text = "$prefix ${dept.assignedUserName ?: "Chưa cập nhật"}"

            val iconRes = when {
                dept.name.contains("IT", true) -> android.R.drawable.ic_menu_manage
                dept.name.contains("Kế toán", true) -> android.R.drawable.ic_menu_agenda
                else -> android.R.drawable.ic_menu_myplaces
            }
            ivDeptIcon.setImageResource(iconRes)

            layoutExpand.removeAllViews()

            if (dept.isExpanded) {
                layoutExpand.visibility = View.VISIBLE
                ivArrow.rotation = 180f

                val titleView = TextView(itemView.context)
                titleView.text = "Nhân sự (${dept.employees.size})"
                titleView.setTextColor(Color.parseColor("#2196F3"))
                titleView.typeface = Typeface.DEFAULT_BOLD
                titleView.setPadding(0, 0, 0, 24)
                layoutExpand.addView(titleView)

                for (emp in dept.employees) {
                    val empView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_employee, layoutExpand, false)

                    empView.findViewById<TextView>(R.id.tvEmployeeName).text = emp.name
                    empView.findViewById<TextView>(R.id.tvEmployeeRole).text = emp.role

                    val tvAvatar = empView.findViewById<TextView>(R.id.tvAvatar)
                    val cardAvatar = tvAvatar.parent as? CardView
                    val firstLetter = if (emp.name.isNotEmpty()) emp.name.substring(0, 1).uppercase() else "?"
                    tvAvatar.text = firstLetter
                    if (cardAvatar != null) {
                        val colorIndex = abs(emp.name.hashCode()) % avatarColors.size
                        cardAvatar.setCardBackgroundColor(Color.parseColor(avatarColors[colorIndex]))
                    }

                    // Click vào nhân viên
                    empView.setOnClickListener {
                        Toast.makeText(itemView.context, "Chọn nhân viên: ${emp.name}", Toast.LENGTH_SHORT).show()
                    }

                    // Nút gọi nhỏ (nếu có id ivCallSmall)
                    val btnCallSmall = empView.findViewById<View>(R.id.ivCallSmall)
                    btnCallSmall?.setOnClickListener {
                        Toast.makeText(itemView.context, "Đang gọi ${emp.name}...", Toast.LENGTH_SHORT).show()
                    }

                    layoutExpand.addView(empView)
                }
            } else {
                layoutExpand.visibility = View.GONE
                ivArrow.rotation = 0f
            }

            layoutHeader.setOnClickListener {
                dept.isExpanded = !dept.isExpanded
                notifyItemChanged(adapterPosition)
            }
        }
    }
}