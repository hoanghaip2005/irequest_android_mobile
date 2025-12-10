package com.project.irequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.irequest.data.models.Employee

class EmployeeAssetsFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_employee_assets, container, false)
        
        employee?.let {
            view.findViewById<TextView>(R.id.tvAssets).text = "Tài sản được cấp: ${it.assets}"
        }
        
        return view
    }

    companion object {
        fun newInstance(employee: Employee) =
            EmployeeAssetsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("employee", employee)
                }
            }
    }
}