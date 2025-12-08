package com.project.irequest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateData(view)
    }

    private fun populateData(view: View) {
        // Personal Info
        setInfoRow(view.findViewById(R.id.info_phone), "Số điện thoại", "0912 345 678")
        setInfoRow(view.findViewById(R.id.info_gender), "Giới tính", "Nam")
        setInfoRow(view.findViewById(R.id.info_cccd), "CCCD", "0123456789123")
        setInfoRow(view.findViewById(R.id.info_marital_status), "Tình trạng", "Độc thân")
        setInfoRow(view.findViewById(R.id.info_email), "Email", "hoang.hai@company.com")
        setInfoRow(view.findViewById(R.id.info_dob), "Ngày sinh", "20/05/2000")
        setInfoRow(view.findViewById(R.id.info_address), "Địa chỉ", "123 Đường ABC, Quận 1, TP.HCM")
        setInfoRow(view.findViewById(R.id.info_bank), "Tài khoản ngân hàng", "VCB - Chi nhánh Sài Gòn")

        // Job Info
        setInfoRow(view.findViewById(R.id.job_position), "Vị trí", "Lập trình viên Android")
        setInfoRow(view.findViewById(R.id.job_status), "Tình trạng HĐ", "Nhân viên chính thức - fulltime")
        setInfoRow(view.findViewById(R.id.job_type), "Loại NV", "Nhân viên chính thức")
        setInfoRow(view.findViewById(R.id.job_department), "Phòng ban", "Công nghệ")
        setInfoRow(view.findViewById(R.id.job_manager), "Quản lý", "Nguyễn Văn A")
        setInfoRow(view.findViewById(R.id.job_branch), "Chi nhánh", "Văn phòng chính")
        setInfoRow(view.findViewById(R.id.job_start_date), "Ngày vào", "01/01/2023")
        setInfoRow(view.findViewById(R.id.job_payment_form), "Hình thức trả lương", "Chuyển khoản")
        setInfoRow(view.findViewById(R.id.job_notes), "Ghi chú", "Năng nổ, nhiệt tình")
    }

    private fun setInfoRow(view: View, label: String, value: String) {
        val labelTextView = view.findViewById<TextView>(R.id.tv_info_label)
        val valueTextView = view.findViewById<TextView>(R.id.tv_info_value)
        labelTextView.text = label
        valueTextView.text = value
    }
}
