package com.project.irequest

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.irequest.data.models.Employee
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddEditEmployeeActivity : BaseActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var etName: TextInputEditText
    private lateinit var etRole: TextInputEditText
    private lateinit var etDepartment: TextInputEditText
    private lateinit var etDateOfBirth: TextInputEditText
    private lateinit var etJoinDate: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText

    private var isEditMode = false
    private var currentEmployee: Employee? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_employee)

        initViews()
        setupDatePickers()
        
        currentEmployee = intent.getParcelableExtra("employee")
        isEditMode = currentEmployee != null
        
        if (isEditMode) {
            tvTitle.text = "Chỉnh Sửa Nhân Viên"
            fillEmployeeData()
        } else {
            tvTitle.text = "Thêm Nhân Viên"
        }
        
        setupClickListeners()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        btnSave = findViewById(R.id.btnSave)
        etName = findViewById(R.id.etName)
        etRole = findViewById(R.id.etRole)
        etDepartment = findViewById(R.id.etDepartment)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        etJoinDate = findViewById(R.id.etJoinDate)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        
        etDateOfBirth.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                etDateOfBirth.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR) - 25, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etJoinDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                etJoinDate.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            if (validateInput()) {
                saveEmployee()
            }
        }
    }

    private fun validateInput(): Boolean {
        if (etName.text.isNullOrBlank()) {
            etName.error = "Vui lòng nhập họ tên"
            etName.requestFocus()
            return false
        }
        if (etRole.text.isNullOrBlank()) {
            etRole.error = "Vui lòng nhập chức vụ"
            return false
        }
        if (etDepartment.text.isNullOrBlank()) {
            etDepartment.error = "Vui lòng nhập phòng ban"
            return false
        }
        return true
    }

    private fun fillEmployeeData() {
        currentEmployee?.let { employee ->
            etName.setText(employee.name)
            etRole.setText(employee.role)
            etDepartment.setText(employee.department)
            etDateOfBirth.setText(employee.dateOfBirth)
            etJoinDate.setText(employee.joinDate)
            etEmail.setText(employee.email)
            etPhone.setText(employee.phone)
            etAddress.setText(employee.address)
        }
    }

    private fun saveEmployee() {
        val employee = Employee(
            id = currentEmployee?.id ?: UUID.randomUUID().toString(),
            name = etName.text.toString(),
            role = etRole.text.toString(),
            department = etDepartment.text.toString(),
            email = etEmail.text.toString(),
            phone = etPhone.text.toString(),
            avatar = "",
            joinDate = etJoinDate.text.toString(),
            status = "Đang làm việc",
            address = etAddress.text.toString(),
            dateOfBirth = etDateOfBirth.text.toString()
        )

        val resultIntent = Intent()
        resultIntent.putExtra("employee", employee)
        setResult(Activity.RESULT_OK, resultIntent)
        Toast.makeText(this, "Đã lưu thông tin nhân viên", Toast.LENGTH_SHORT).show()
        finish()
    }
}