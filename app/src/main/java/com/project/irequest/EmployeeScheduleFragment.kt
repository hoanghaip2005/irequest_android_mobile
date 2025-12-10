package com.project.irequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.irequest.data.models.Employee

class EmployeeScheduleFragment : Fragment() {

    private var employee: Employee? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            employee = it.getParcelable("employee")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_employee_schedule, container, false)
        
        employee?.let {
            view.findViewById<TextView>(R.id.tvWorkSchedule).text = "Lịch làm việc: ${it.workSchedule}"
            view.findViewById<TextView>(R.id.tvTotalWorkHours).text = "Tổng ngày giờ: ${it.totalWorkHours}"
        }
        
        return view
    }

    companion object {
        fun newInstance(employee: Employee) =
            EmployeeScheduleFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("employee", employee)
                }
            }
    }
}