package com.project.irequest

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

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

        val process = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_PROCESS", Process::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Process>("EXTRA_PROCESS")
        }

        if (process != null) {
            tvProcessName.text = process.name
            tvProcessStatus.text = "Trạng thái: ${process.status}"
            tvProcessDate.text = "Ngày tạo: ${process.creationDate}"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}