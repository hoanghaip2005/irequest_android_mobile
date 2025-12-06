package com.project.irequest

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SmartAnalysisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_analysis)

        // 1. Nút Back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 2. XỬ LÝ NÚT ĐĂNG KÝ (Giả)
        // Tìm nút theo ID bạn vừa đặt
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister?.setOnClickListener {
            // Hiện thông báo
            Toast.makeText(this, "Đăng ký ca làm thành công!", Toast.LENGTH_SHORT).show()

            // Đổi trạng thái nút để nhìn cho thật
            btnRegister.text = "Đã đăng ký"
            btnRegister.isEnabled = false // Khóa nút lại không cho bấm nữa
            btnRegister.setBackgroundColor(Color.GRAY) // Đổi màu xám
        }
    }
}