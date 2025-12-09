package com.project.irequest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Employee

class EmployeeAdapter(
    private val employees: List<Employee>,
    private val onItemClick: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAvatar: TextView = itemView.findViewById(R.id.tvAvatar)
        val tvEmployeeName: TextView = itemView.findViewById(R.id.tvEmployeeName)
        val tvEmployeeRole: TextView = itemView.findViewById(R.id.tvEmployeeRole)
        val tvEmployeeDepartment: TextView? = itemView.findViewById(R.id.tvEmployeeDepartment)

        fun bind(employee: Employee) {
            // Set avatar with first letter of name
            tvAvatar.text = employee.name.firstOrNull()?.toString()?.uppercase() ?: "?"
            
            // Set random background color for avatar
            val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7B731")
            val colorIndex = (employee.name.hashCode() % colors.size).let { if (it < 0) it + colors.size else it }
            tvAvatar.setBackgroundColor(android.graphics.Color.parseColor(colors[colorIndex]))
            
            tvEmployeeName.text = employee.name
            tvEmployeeRole.text = employee.role
            
            // Set department if TextView exists in layout
            tvEmployeeDepartment?.text = employee.department
            
            itemView.setOnClickListener {
                onItemClick(employee)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(employees[position])
    }

    override fun getItemCount(): Int = employees.size
}
