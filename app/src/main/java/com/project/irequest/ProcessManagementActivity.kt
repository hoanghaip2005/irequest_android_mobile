package com.project.irequest

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project.irequest.ProcessAdapter

class ProcessManagementActivity : AppCompatActivity() {

    private lateinit var rvProcessList: RecyclerView
    private lateinit var fabAddProcess: FloatingActionButton
    private lateinit var processAdapter: ProcessAdapter
    private val processList = mutableListOf<Process>()

    private val addProcessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newProcess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra("NEW_PROCESS", Process::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Process>("NEW_PROCESS")
            }
            if (newProcess != null) {
                processList.add(0, newProcess)
                processAdapter.notifyItemInserted(0)
                rvProcessList.scrollToPosition(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_management)

        val toolbar: Toolbar = findViewById(R.id.toolbar_process_management)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        rvProcessList = findViewById(R.id.rv_process_list)
        fabAddProcess = findViewById(R.id.fab_add_process)

        setupRecyclerView()
        loadInitialData()

        fabAddProcess.setOnClickListener {
            val intent = Intent(this, AddProcessActivity::class.java)
            addProcessLauncher.launch(intent)
        }
    }

    private fun setupRecyclerView() {
        rvProcessList.layoutManager = LinearLayoutManager(this)
        processAdapter = ProcessAdapter(processList)
        rvProcessList.adapter = processAdapter
    }

    private fun loadInitialData() {
        // Dữ liệu mẫu ban đầu - Sử dụng Named Arguments để tránh lỗi lệch tham số
        processList.addAll(listOf(
            Process(
                name = "Quy trình Phê duyệt Đơn hàng", 
                status = "Đang chờ", 
                date = "15/07/2024"
            ),
            Process(
                name = "Quy trình Tuyển dụng Nhân sự", 
                status = "Hoàn thành", 
                date = "12/07/2024"
            ),
            Process(
                name = "Quy trình Hỗ trợ Khách hàng", 
                status = "Đang chờ", 
                date = "11/07/2024"
            ),
            Process(
                name = "Quy trình Thanh toán", 
                status = "Hoàn thành", 
                date = "10/07/2024"
            )
        ))
        processAdapter.notifyDataSetChanged()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
