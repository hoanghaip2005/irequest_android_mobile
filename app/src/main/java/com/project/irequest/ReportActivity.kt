package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// Model dữ liệu
data class ReportData(
    val totalShift: String,
    val totalHours: String,
    val totalOrders: String,
    val score: String,
    val kpiSales: Int,
    val kpiService: Int,
    val kpiTime: Int
)

class ReportActivity : AppCompatActivity() {

    private var isExpanded = false // Biến trạng thái Xem thêm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        initClickEvents()
        loadData()
    }

    private fun initClickEvents() {
        // 1. Nút Back
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // 2. Nút "Phân tích thông minh" (ĐÃ MỞ KHÓA)
        // Tìm View có ID cardSmartAnalysis và set sự kiện
        findViewById<View>(R.id.cardSmartAnalysis).setOnClickListener {
            // Chuyển sang màn hình SmartAnalysisActivity
            val intent = Intent(this, SmartAnalysisActivity::class.java)
            startActivity(intent)
        }

        // 3. Nút "Xem thêm"
        val btnSeeMore = findViewById<TextView>(R.id.btnSeeMore)
        val layoutMoreItems = findViewById<LinearLayout>(R.id.layoutMoreItems)

        btnSeeMore.setOnClickListener {
            isExpanded = !isExpanded // Đảo ngược trạng thái (Đóng <-> Mở)

            if (isExpanded) {
                // Nếu mở -> Hiện layout ẩn, đổi chữ thành "Thu gọn"
                layoutMoreItems.visibility = View.VISIBLE
                btnSeeMore.text = "Thu gọn"
            } else {
                // Nếu đóng -> Ẩn layout, đổi chữ thành "Xem thêm"
                layoutMoreItems.visibility = View.GONE
                btnSeeMore.text = "Xem thêm"
            }
        }
    }

    private fun loadData() {
        // Giả vờ loading
        Handler(Looper.getMainLooper()).postDelayed({
            val data = ReportData(
                totalShift = "20/30",
                totalHours = "160h",
                totalOrders = "45",
                score = "9.2",
                kpiSales = 75,
                kpiService = 90,
                kpiTime = 100
            )
            updateUI(data)
        }, 500)
    }

    private fun updateUI(data: ReportData) {
        try {
            findViewById<TextView>(R.id.tvTotalShift)?.text = data.totalShift
            findViewById<TextView>(R.id.tvTotalHours)?.text = data.totalHours
            findViewById<TextView>(R.id.tvTotalOrders)?.text = data.totalOrders
            findViewById<TextView>(R.id.tvScore)?.text = data.score

            findViewById<ProgressBar>(R.id.pbKpiSales)?.progress = data.kpiSales
            findViewById<ProgressBar>(R.id.pbKpiService)?.progress = data.kpiService
            findViewById<ProgressBar>(R.id.pbKpiTime)?.progress = data.kpiTime
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}