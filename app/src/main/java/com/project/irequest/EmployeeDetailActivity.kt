package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.example.irequest.data.models.Employee
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class EmployeeDetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_EMPLOYEE = "employee"
        const val EXTRA_EMPLOYEE_NAME = "employee_name"
        const val EXTRA_EMPLOYEE_ROLE = "employee_role"
        const val EXTRA_DEPARTMENT_NAME = "department_name"
    }

    private lateinit var employee: Employee
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val editEmployeeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedEmployee = result.data?.getParcelableExtra<Employee>(EXTRA_EMPLOYEE)
            if (updatedEmployee != null) {
                employee = updatedEmployee
                displayEmployeeHeaderInfo()
                setupViewPager() // Re-setup viewpager to pass updated data to fragments
                
                // Pass back the updated employee to the list
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_EMPLOYEE, employee)
                setResult(Activity.RESULT_OK, resultIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_detail)

        employee = intent.getParcelableExtra(EXTRA_EMPLOYEE) ?: return

        initViews()
        displayEmployeeHeaderInfo()
        setupViewPager()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        findViewById<ImageView>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, AddEditEmployeeActivity::class.java)
            intent.putExtra(EXTRA_EMPLOYEE, employee)
            editEmployeeLauncher.launch(intent)
        }
    }

    private fun displayEmployeeHeaderInfo() {
        // Avatar
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)
        tvAvatar.text = employee.name.firstOrNull()?.toString()?.uppercase() ?: "?"
        
        val colors = listOf("#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A", "#98D8C8", "#F7B731")
        val colorIndex = (employee.name.hashCode() % colors.size).let { if (it < 0) it + colors.size else it }
        tvAvatar.setBackgroundColor(android.graphics.Color.parseColor(colors[colorIndex]))

        // Basic Info in header
        findViewById<TextView>(R.id.tvName).text = employee.name
        findViewById<TextView>(R.id.tvRole).text = employee.role
        findViewById<TextView>(R.id.tvDepartment).text = employee.department
        findViewById<TextView>(R.id.tvStatus).text = employee.status
    }
    
    private fun setupViewPager() {
        val adapter = EmployeeDetailPagerAdapter(this, employee)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Thông tin"
                1 -> "Lịch"
                2 -> "Tài sản"
                else -> null
            }
        }.attach()
    }
}
