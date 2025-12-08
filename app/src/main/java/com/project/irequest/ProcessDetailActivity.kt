package com.project.irequest

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProcessDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar_process_detail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val tvProcessName: TextView = findViewById(R.id.tv_detail_process_name)
        val tvProcessStatus: TextView = findViewById(R.id.tv_detail_process_status)
        val tvProcessDate: TextView = findViewById(R.id.tv_detail_process_date)
        val rvProcessSteps: RecyclerView = findViewById(R.id.rv_process_steps)

        // Lấy đối tượng Process từ Intent
        val process = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_PROCESS", Process::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Process>("EXTRA_PROCESS")
        }

        // Hiển thị thông tin quy trình
        if (process != null) {
            supportActionBar?.title = process.name // Cập nhật tiêu đề toolbar
            tvProcessName.text = process.name
            tvProcessStatus.text = "Trạng thái: ${process.status}"
            tvProcessDate.text = "Ngày tạo: ${process.date}"
        }

        // --- Setup for Process Steps RecyclerView ---
        // (Dữ liệu mẫu, bạn sẽ thay thế bằng logic lấy dữ liệu thật)
        val steps = listOf(
            ProcessStep("Bước 1: Gửi yêu cầu", "Người dùng tạo và gửi yêu cầu phê duyệt.", "15/07/2024", StepStatus.COMPLETED),
            ProcessStep("Bước 2: Trưởng phòng duyệt", "Trưởng phòng xem xét và phê duyệt yêu cầu.", "16/07/2024", StepStatus.COMPLETED),
            ProcessStep("Bước 3: Ban giám đốc duyệt", "Ban giám đốc xem xét và phê duyệt cuối cùng.", "Đang chờ duyệt", StepStatus.CURRENT),
            ProcessStep("Bước 4: Hoàn tất", "Quy trình kết thúc và thông báo cho người dùng.", "", StepStatus.UPCOMING)
        )

        // Setup RecyclerView
        rvProcessSteps.layoutManager = LinearLayoutManager(this)
        rvProcessSteps.adapter = ProcessStepAdapter(steps)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
