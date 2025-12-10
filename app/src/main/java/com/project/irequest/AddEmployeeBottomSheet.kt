package com.project.irequest

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.example.irequest.data.models.Employee
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class AddEmployeeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etRole: TextInputEditText
    private lateinit var etDepartment: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnAdd: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private var callback: ((Employee) -> Unit)? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        return inflater.inflate(R.layout.bottom_sheet_add_employee, container, false)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etName = view.findViewById(R.id.etName)
        etRole = view.findViewById(R.id.etRole)
        etDepartment = view.findViewById(R.id.etDepartment)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        btnAdd = view.findViewById(R.id.btnAdd)
        btnCancel = view.findViewById(R.id.btnCancel)

        btnAdd.setOnClickListener {
            saveEmployee()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun saveEmployee() {
        val name = etName.text.toString().trim()
        val role = etRole.text.toString().trim()
        val department = etDepartment.text.toString().trim()

        if (name.isEmpty() || role.isEmpty() || department.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Vui lòng nhập đầy đủ: Tên, Chức vụ, Phòng ban",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val newEmployee = Employee(
            id = UUID.randomUUID().toString(),
            name = name,
            role = role,
            department = department,
            email = etEmail.text.toString().trim(),
            phone = etPhone.text.toString().trim()
        )

        callback?.invoke(newEmployee)
        Toast.makeText(requireContext(), "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    fun setOnEmployeeAddedCallback(callback: (Employee) -> Unit) {
        this.callback = callback
    }

    companion object {
        const val TAG = "AddEmployeeBottomSheet"
    }
}
