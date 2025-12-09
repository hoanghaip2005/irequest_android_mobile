package com.project.irequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.irequest.data.models.Employee

class EmployeeInfoFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_employee_info, container, false)
        
        employee?.let {
            view.findViewById<TextView>(R.id.tvPhoneNumber).text = "Số điện thoại: ${it.phone}"
            view.findViewById<TextView>(R.id.tvGender).text = "Giới tính: ${it.gender}"
            view.findViewById<TextView>(R.id.tvCccd).text = "CCCD: ${it.cccd}"
            view.findViewById<TextView>(R.id.tvEmail).text = "Email: ${it.email}"
            view.findViewById<TextView>(R.id.tvDateOfBirth).text = "Ngày sinh: ${it.dateOfBirth}"
            view.findViewById<TextView>(R.id.tvAddress).text = "Địa chỉ: ${it.address}"
            view.findViewById<TextView>(R.id.tvBankAccount).text = "Tài khoản ngân hàng: ${it.bankAccount}"

            view.findViewById<TextView>(R.id.tvPosition).text = "Vị trí: ${it.role}"
            view.findViewById<TextView>(R.id.tvContractStatus).text = "Tình trạng hợp đồng: ${it.contractStatus}"
            view.findViewById<TextView>(R.id.tvEmployeeType).text = "Loại nhân viên: ${it.employeeType}"
            view.findViewById<TextView>(R.id.tvDepartment).text = "Phòng ban: ${it.department}"
            view.findViewById<TextView>(R.id.tvManager).text = "Quản lí: ${it.manager}"
            view.findViewById<TextView>(R.id.tvBranch).text = "Chi nhánh: ${it.branch}"
            view.findViewById<TextView>(R.id.tvJoinDate).text = "Ngày vào: ${it.joinDate}"
            view.findViewById<TextView>(R.id.tvPaymentMethod).text = "Hình thức trả lương: ${it.paymentMethod}"
            view.findViewById<TextView>(R.id.tvNotes).text = "Ghi chú: ${it.notes}"

            view.findViewById<TextView>(R.id.tvCheckIn).text = "Check in: ${it.checkIn}"
            view.findViewById<TextView>(R.id.tvCheckOut).text = "Check out: ${it.checkOut}"
            view.findViewById<TextView>(R.id.tvShiftRegister).text = "Đăng kí ca: ${it.shiftRegister}"
        }
        
        return view
    }

    companion object {
        fun newInstance(employee: Employee) =
            EmployeeInfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("employee", employee)
                }
            }
    }
}
