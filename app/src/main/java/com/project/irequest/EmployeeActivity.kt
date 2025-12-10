package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Employee
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmployeeActivity : BaseActivity() {

    private lateinit var rvEmployees: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var emptyState: View
    private lateinit var employeeAdapter: EmployeeAdapter
    private var allEmployees = mutableListOf<Employee>()
    private var filteredEmployees = mutableListOf<Employee>()

    private val addEmployeeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newEmployee = result.data?.getParcelableExtra<Employee>("employee")
            if (newEmployee != null) {
                allEmployees.add(0, newEmployee)
                filterEmployees(etSearch.text.toString())
            }
        }
    }
    
    private val editEmployeeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val updatedEmployee = result.data?.getParcelableExtra<Employee>("employee")
            if (updatedEmployee != null) {
                val index = allEmployees.indexOfFirst { it.id == updatedEmployee.id }
                if (index != -1) {
                    allEmployees[index] = updatedEmployee
                    filterEmployees(etSearch.text.toString())
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee)

        initViews()
        setupRecyclerView()
        setupSearchBar()
        loadEmployees()
    }

    private fun initViews() {
        rvEmployees = findViewById(R.id.rvEmployees)
        etSearch = findViewById(R.id.etSearch)
        emptyState = findViewById(R.id.emptyState)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnFilter).setOnClickListener {
            // Show filter bottom sheet
            FilterBottomSheet.show(supportFragmentManager) { options ->
                applyFilters(options)
            }
        }

        findViewById<FloatingActionButton>(R.id.fabAddEmployee).setOnClickListener {
            showAddEmployeeBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter(filteredEmployees) { employee ->
            val intent = Intent(this, EmployeeDetailActivity::class.java)
            intent.putExtra("employee", employee)
            editEmployeeLauncher.launch(intent)
        }
        
        rvEmployees.apply {
            layoutManager = LinearLayoutManager(this@EmployeeActivity)
            adapter = employeeAdapter
        }
    }

    private fun setupSearchBar() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterEmployees(s.toString())
            }
        })
    }

    private fun filterEmployees(query: String) {
        filteredEmployees.clear()
        if (query.isEmpty()) {
            filteredEmployees.addAll(allEmployees)
        } else {
            val searchQuery = query.lowercase()
            filteredEmployees.addAll(
                allEmployees.filter { 
                    it.name.lowercase().contains(searchQuery) ||
                    it.role.lowercase().contains(searchQuery) ||
                    it.department.lowercase().contains(searchQuery)
                }
            )
        }
        employeeAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun updateEmptyState() {
        if (filteredEmployees.isEmpty()) {
            rvEmployees.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
        } else {
            rvEmployees.visibility = View.VISIBLE
            emptyState.visibility = View.GONE
        }
    }

    private fun loadEmployees() {
        // Sample data - replace with actual data from API/Database
        allEmployees = mutableListOf(
            Employee(
                id = "EMP001",
                name = "Hoàng Hải",
                role = "Android Developer",
                department = "Phòng Công Nghệ",
                email = "hoanghai@company.com",
                phone = "0123456789",
                avatar = "",
                joinDate = "01/01/2023",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "01/01/1995"
            ),
            Employee(
                id = "EMP002",
                name = "Trần Văn CEO",
                role = "Tổng Giám Đốc",
                department = "Ban Giám Đốc",
                email = "ceo@company.com",
                phone = "0987654321",
                avatar = "",
                joinDate = "01/01/2020",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "15/05/1980"
            ),
            Employee(
                id = "EMP003",
                name = "Lê Thư Ký",
                role = "Thư ký TGĐ",
                department = "Ban Giám Đốc",
                email = "secretary@company.com",
                phone = "0123456788",
                avatar = "",
                joinDate = "01/06/2021",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "20/08/1992"
            ),
            Employee(
                id = "EMP004",
                name = "Lê Văn Code",
                role = "Trưởng phòng",
                department = "Phòng Công Nghệ",
                email = "levancode@company.com",
                phone = "0123456787",
                avatar = "",
                joinDate = "01/03/2021",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "10/12/1988"
            ),
            Employee(
                id = "EMP005",
                name = "Nguyễn Fullstack",
                role = "Senior Dev",
                department = "Phòng Công Nghệ",
                email = "fullstack@company.com",
                phone = "0123456786",
                avatar = "",
                joinDate = "15/07/2022",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "25/03/1990"
            ),
            Employee(
                id = "EMP006",
                name = "Trần Mobile",
                role = "Android Dev",
                department = "Phòng Công Nghệ",
                email = "mobile@company.com",
                phone = "0123456785",
                avatar = "",
                joinDate = "01/09/2022",
                status = "Đang làm việc",
                address = "Hà Nội",
                dateOfBirth = "30/11/1993"
            )
        )
        
        filteredEmployees.addAll(allEmployees)
        employeeAdapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun showAddEmployeeBottomSheet() {
        val bottomSheet = AddEmployeeBottomSheet()
        bottomSheet.setOnEmployeeAddedCallback { newEmployee ->
            allEmployees.add(0, newEmployee)
            filterEmployees(etSearch.text.toString())
        }
        bottomSheet.show(supportFragmentManager, AddEmployeeBottomSheet.TAG)
    }

    private fun applyFilters(options: FilterOptions) {
        // Start from the full list, then apply search and filters
        val query = etSearch.text.toString().trim().lowercase()

        var working = allEmployees.asSequence()

        // Branch filter -> here we assume department stores branch name if used
        options.branch?.let { b ->
            working = working.filter { it.department.contains(b, ignoreCase = true) }
        }

        // Shift filter -> no shift field in Employee model; skip or map if available
        options.shift?.let { s ->
            // placeholder: try to match role or address
            working = working.filter { it.role.contains(s, ignoreCase = true) || it.address.contains(s, ignoreCase = true) }
        }

        // Employee types -> check role/status
        if (options.employeeTypes.isNotEmpty()) {
            working = working.filter { emp ->
                options.employeeTypes.any { type ->
                    emp.role.contains(type, ignoreCase = true) || emp.status.contains(type, ignoreCase = true)
                }
            }
        }

        // Contract status
        if (options.contractStatus.isNotEmpty()) {
            working = working.filter { emp ->
                options.contractStatus.any { cs -> emp.status.contains(cs, ignoreCase = true) }
            }
        }

        // Apply search query
        if (query.isNotEmpty()) {
            working = working.filter { emp ->
                emp.name.lowercase().contains(query) || emp.role.lowercase().contains(query) || emp.department.lowercase().contains(query)
            }
        }

        // Convert sequence to list
        val result = working.toMutableList()

        // Join order: attempt to sort by joinDate string; naive approach
        when (options.joinOrder) {
            "newest" -> result.sortByDescending { it.joinDate }
            "oldest" -> result.sortBy { it.joinDate }
        }

        // KPI sort placeholder - not implemented (no KPI numeric field in model)

        // Update filtered list and adapter
        filteredEmployees.clear()
        filteredEmployees.addAll(result)
        employeeAdapter.notifyDataSetChanged()
        updateEmptyState()
    }
}
