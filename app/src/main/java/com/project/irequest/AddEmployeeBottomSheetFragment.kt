package com.project.irequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.irequest.data.models.Employee
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class AddEmployeeBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AddEmployeeBottomSheetFragment"
    }

    private var callback: ((Employee) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etRole = view.findViewById<TextInputEditText>(R.id.etRole)
        val etDepartment = view.findViewById<TextInputEditText>(R.id.etDepartment)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAdd)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val role = etRole.text.toString().trim()
            val department = etDepartment.text.toString().trim()
            
            if (name.isEmpty() || role.isEmpty() || department.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ: Tên, Chức vụ, Phòng ban", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
            Toast.makeText(context, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    fun setOnEmployeeAddedCallback(callback: (Employee) -> Unit) {
        this.callback = callback
    }
}
