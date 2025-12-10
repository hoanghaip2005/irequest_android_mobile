package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.repository.FirebaseReportRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ReportActivity : AppCompatActivity() {

    private val reportRepository = FirebaseReportRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var activityAdapter: RequestActivityAdapter
    
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        setupRecyclerView()
        initClickEvents()
        initDefaultValues()
        loadDataFromFirebase()
    }
    
    private fun initDefaultValues() {
        // Hiển thị giá trị mặc định thay vì "..."
        findViewById<TextView>(R.id.tvTotalCreated)?.text = "0"
        findViewById<TextView>(R.id.tvTotalCompleted)?.text = "0"
        findViewById<TextView>(R.id.tvTotalProcessing)?.text = "0"
        findViewById<TextView>(R.id.tvCompletionRate)?.text = "0%"
        
        findViewById<ProgressBar>(R.id.pbKpiHigh)?.progress = 0
        findViewById<ProgressBar>(R.id.pbKpiMedium)?.progress = 0
        findViewById<ProgressBar>(R.id.pbKpiTime)?.progress = 0
    }

    private fun setupRecyclerView() {
        val rvActivities = findViewById<RecyclerView>(R.id.rvActivities)
        activityAdapter = RequestActivityAdapter()
        rvActivities.layoutManager = LinearLayoutManager(this)
        rvActivities.adapter = activityAdapter
    }

    private fun initClickEvents() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.cardSmartAnalysis).setOnClickListener {
            val intent = Intent(this, SmartAnalysisActivity::class.java)
            startActivity(intent)
        }

        // Ẩn nút Xem thêm/Thu gọn vì không có chức năng
        findViewById<TextView>(R.id.btnSeeMore)?.visibility = View.GONE
    }

    private fun loadDataFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showLoading(false)
            showEmptyState()
            return
        }

        showLoading(true)
        
        lifecycleScope.launch {
            try {
                // Load monthly stats
                val statsResult = reportRepository.getMonthlyRequestStats(userId)
                statsResult.onSuccess { stats ->
                    updateStatsUI(stats)
                }.onFailure { e ->
                    e.printStackTrace()
                    // Giữ giá trị mặc định nếu lỗi
                }

                // Load priority stats for KPI
                val priorityResult = reportRepository.getPriorityStats(userId)
                priorityResult.onSuccess { priority ->
                    updateKpiUI(priority)
                }.onFailure { e ->
                    e.printStackTrace()
                }

                // Load processing time stats
                val timeResult = reportRepository.getAverageProcessingTime(userId)
                timeResult.onSuccess { time ->
                    updateTimeKpiUI(time)
                }.onFailure { e ->
                    e.printStackTrace()
                }

                // Load recent activities
                val activitiesResult = reportRepository.getRecentActivities(userId, limit = 10)
                activitiesResult.onSuccess { activities ->
                    showLoading(false)
                    if (activities.isEmpty()) {
                        showEmptyState()
                    } else {
                        showActivities(activities)
                    }
                }.onFailure { e ->
                    showLoading(false)
                    showEmptyState()
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                showLoading(false)
                showEmptyState()
                e.printStackTrace()
            }
        }
    }

    private fun updateStatsUI(stats: com.example.irequest.data.repository.MonthlyRequestStats) {
        findViewById<TextView>(R.id.tvTotalCreated)?.text = "${stats.totalCreated}"
        findViewById<TextView>(R.id.tvTotalCompleted)?.text = "${stats.totalCompleted}"
        findViewById<TextView>(R.id.tvTotalProcessing)?.text = "${stats.totalProcessing}"
        findViewById<TextView>(R.id.tvCompletionRate)?.text = "${stats.completionRate}%"
    }

    private fun updateKpiUI(priority: com.example.irequest.data.repository.PriorityStats) {
        val total = priority.high + priority.medium + priority.low
        if (total > 0) {
            val highPercent = (priority.high.toFloat() / total * 100).toInt()
            val mediumPercent = (priority.medium.toFloat() / total * 100).toInt()
            
            findViewById<ProgressBar>(R.id.pbKpiHigh)?.progress = highPercent
            findViewById<ProgressBar>(R.id.pbKpiMedium)?.progress = mediumPercent
        }
    }

    private fun updateTimeKpiUI(time: com.example.irequest.data.repository.ProcessingTimeStats) {
        // Mục tiêu < 3 ngày, càng thấp càng tốt
        val progress = if (time.averageDays <= 3) 100 else maxOf(0, 100 - (time.averageDays - 3) * 20)
        findViewById<ProgressBar>(R.id.pbKpiTime)?.progress = progress
    }

    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.pbLoading)?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState() {
        findViewById<TextView>(R.id.tvEmptyState)?.visibility = View.VISIBLE
        findViewById<RecyclerView>(R.id.rvActivities)?.visibility = View.GONE
        
        // Show sample cards instead
        findViewById<View>(R.id.cardSample1)?.visibility = View.VISIBLE
        findViewById<View>(R.id.cardSample2)?.visibility = View.VISIBLE
        findViewById<View>(R.id.cardSample3)?.visibility = View.VISIBLE
    }

    private fun showActivities(activities: List<com.example.irequest.data.repository.RequestActivity>) {
        findViewById<TextView>(R.id.tvEmptyState)?.visibility = View.GONE
        findViewById<RecyclerView>(R.id.rvActivities)?.visibility = View.VISIBLE
        
        // Hide sample cards
        findViewById<View>(R.id.cardSample1)?.visibility = View.GONE
        findViewById<View>(R.id.cardSample2)?.visibility = View.GONE
        findViewById<View>(R.id.cardSample3)?.visibility = View.GONE
        
        activityAdapter.updateData(activities)
    }
}