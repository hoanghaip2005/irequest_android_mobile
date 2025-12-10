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

        // 2. XỬ LÝ NÚT XEM DANH SÁCH YÊU CẦU ƯU TIÊN
        val btnViewPriority = findViewById<Button>(R.id.btnViewPriority)

        btnViewPriority?.setOnClickListener {
            // Hiện thông báo
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
        }
    }
}