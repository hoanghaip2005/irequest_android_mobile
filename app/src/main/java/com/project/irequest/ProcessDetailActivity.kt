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
            ProcessStep(
                stepId = "",
                workflowId = "",
                title = "Bước 1: Gửi yêu cầu",
                description = "Người dùng tạo và gửi yêu cầu phê duyệt.",
                date = "15/07/2024",
                status = StepStatus.COMPLETED
            ),
            ProcessStep(
                stepId = "",
                workflowId = "",
                title = "Bước 2: Trưởng phòng duyệt",
                description = "Trưởng phòng xem xét và phê duyệt yêu cầu.",
                date = "16/07/2024",
                status = StepStatus.COMPLETED
            ),
            ProcessStep(
                stepId = "",
                workflowId = "",
                title = "Bước 3: Ban giám đốc duyệt",
                description = "Ban giám đốc xem xét và phê duyệt cuối cùng.",
                date = "Đang chờ duyệt",
                status = StepStatus.CURRENT
            ),
            ProcessStep(
                stepId = "",
                workflowId = "",
                title = "Bước 4: Hoàn tất",
                description = "Quy trình kết thúc và thông báo cho người dùng.",
                date = "",
                status = StepStatus.UPCOMING
            )
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
