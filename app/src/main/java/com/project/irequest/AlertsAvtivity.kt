package com.project.irequest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class AlertsActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvAlerts: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSubHeader: TextView
    private lateinit var tvEmptyState: TextView
    private lateinit var btnMarkAllRead: ImageView

    private lateinit var adapter: AlertsAdapter
    private val allAlerts = mutableListOf<AlertData>()
    private var displayedAlerts = mutableListOf<AlertData>()

    // Biến lưu trạng thái Tab hiện tại để khi undo/xóa thì không bị nhảy tab
    private var currentTabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        initViews()
        setupRecyclerView()
        setupSwipeToDelete()
        loadData()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        rvAlerts = findViewById(R.id.rvAlerts)
        progressBar = findViewById(R.id.progressBar)
        tvSubHeader = findViewById(R.id.tvSubHeader)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead)

        val tabs = listOf("Tất cả", "Thông tin chung", "Cảnh báo SLA")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabIndex = tab?.position ?: 0
                filterData(currentTabIndex)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 👇 TÍNH NĂNG: Đánh dấu tất cả đã đọc
        btnMarkAllRead.setOnClickListener {
            if (displayedAlerts.isEmpty()) return@setOnClickListener

            // Cập nhật dữ liệu gốc
            allAlerts.forEach { it.isRead = true }
            // Cập nhật list đang hiện
            displayedAlerts.forEach { it.isRead = true }

            adapter.notifyDataSetChanged()
            updateHeaderCount()
            Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        // 👇 TÍNH NĂNG: Click hiện BottomSheet
        adapter = AlertsAdapter(displayedAlerts) { alert ->
            showDetailBottomSheet(alert)

            // Tự động đánh dấu đã đọc khi click
            if (!alert.isRead) {
                alert.isRead = true
                adapter.notifyDataSetChanged()
                updateHeaderCount()
            }
        }
        rvAlerts.layoutManager = LinearLayoutManager(this)
        rvAlerts.adapter = adapter
    }

    // 👇 TÍNH NĂNG: Bottom Sheet Dialog
    private fun showDetailBottomSheet(alert: AlertData) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_alert_detail, null)

        view.findViewById<TextView>(R.id.tvDetailTitle).text = alert.title
        view.findViewById<TextView>(R.id.tvDetailTime).text = alert.time
        view.findViewById<TextView>(R.id.tvDetailMessage).text = alert.message

        val btnAction = view.findViewById<Button>(R.id.btnDetailAction)

        btnAction.setOnClickListener {
            Toast.makeText(this, "Đang mở yêu cầu...", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // 👇 TÍNH NĂNG: Vuốt xóa + Hoàn tác (Undo)
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(r: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemToDelete = adapter.getItem(position)

                // 1. Xóa khỏi UI
                adapter.removeItem(position)

                // 2. Xóa khỏi list quản lý
                displayedAlerts.remove(itemToDelete) // Xóa khỏi list hiển thị
                allAlerts.remove(itemToDelete)       // Xóa khỏi list gốc

                updateHeaderCount()

                // 3. Hiện Snackbar cho phép Hoàn tác
                val snackbar = Snackbar.make(rvAlerts, "Đã xóa 1 thông báo", Snackbar.LENGTH_LONG)
                snackbar.setAction("HOÀN TÁC") {
                    // Nếu bấm Hoàn tác -> Thêm lại
                    allAlerts.add(itemToDelete)
                    // Cần tính toán lại vị trí trong displayedAlerts (để đơn giản ta reload filter)
                    filterData(currentTabIndex)
                    rvAlerts.scrollToPosition(position)
                }
                // Set màu cho nút Hoàn tác (Màu xanh)
                snackbar.setActionTextColor(resources.getColor(R.color.primary_blue, null))
                snackbar.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvAlerts)
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
        rvAlerts.visibility = View.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            allAlerts.clear()
            allAlerts.addAll(generateInitialData())

            progressBar.visibility = View.GONE
            rvAlerts.visibility = View.VISIBLE

            filterData(0)
        }, 1000)
    }

    private fun filterData(tabIndex: Int) {
        val filtered = when (tabIndex) {
            1 -> allAlerts.filter {
                it.type == AlertType.REQUEST_UPDATE ||
                        it.type == AlertType.INFO ||
                        it.type == AlertType.REQUEST_APPROVED
            }
            2 -> allAlerts.filter { it.type == AlertType.SLA_WARNING }
            else -> allAlerts
        }

        displayedAlerts.clear()
        displayedAlerts.addAll(filtered)
        adapter.notifyDataSetChanged()

        if (displayedAlerts.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvAlerts.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvAlerts.visibility = View.VISIBLE
        }
        updateHeaderCount()
    }

    private fun updateHeaderCount() {
        val unreadCount = displayedAlerts.count { !it.isRead }
        tvSubHeader.text = if (unreadCount > 0) "$unreadCount tin chưa đọc" else "Đã đọc hết"
    }

    private fun generateInitialData(): List<AlertData> {
        val list = mutableListOf<AlertData>()

        list.add(AlertData(
            type = AlertType.SLA_WARNING,
            title = "Sắp hết hạn",
            message = "Yêu cầu #REQ-001 cần xử lý gấp trước 17:00 chiều nay.",
            time = "Còn 1h",
            isRead = false,
            group = "Hôm nay",
            badgeText = "Khẩn cấp"
        ))

        for (i in 1..3) {
            list.add(AlertData(
                type = AlertType.REQUEST_UPDATE,
                title = "Cập nhật yêu cầu",
                message = "Yêu cầu #REQ-00${i+1} đã được chuyển sang phòng kế toán.",
                time = "${i+1} giờ trước",
                isRead = false,
                group = "Hôm nay"
            ))
        }

        for (i in 0..5) {
            list.add(AlertData(
                type = AlertType.REQUEST_APPROVED,
                title = "Yêu cầu được duyệt",
                message = "Trưởng phòng đã duyệt yêu cầu nghỉ phép #REQ-OLD-$i của bạn.",
                time = "1 ngày trước",
                isRead = true,
                group = "Hôm qua"
            ))
        }
        return list
    }
}