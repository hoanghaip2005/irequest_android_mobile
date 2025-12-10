package com.project.irequest

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

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

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

            lifecycleScope.launch {
                try {
                    // Cập nhật Firebase
                    val userId = auth.currentUser?.uid ?: return@launch
                    val batch = firestore.batch()
                    
                    displayedAlerts.filter { !it.isRead }.forEach { alert ->
                        val docRef = firestore.collection("notifications").document(alert.id)
                        batch.update(docRef, "isRead", true)
                    }
                    
                    batch.commit().await()
                    
                    // Cập nhật UI
                    allAlerts.forEach { it.isRead = true }
                    displayedAlerts.forEach { it.isRead = true }

                    adapter.notifyDataSetChanged()
                    updateHeaderCount()
                    Toast.makeText(this@AlertsActivity, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@AlertsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
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
                
                // Cập nhật Firebase
                lifecycleScope.launch {
                    try {
                        firestore.collection("notifications")
                            .document(alert.id)
                            .update("isRead", true)
                            .await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
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
                    filterData(currentTabIndex)
                    rvAlerts.scrollToPosition(position)
                    
                    // Hoàn tác trong Firebase
                    lifecycleScope.launch {
                        try {
                            val notifMap = mapOf(
                                "userId" to (auth.currentUser?.uid ?: ""),
                                "type" to itemToDelete.type.name,
                                "title" to itemToDelete.title,
                                "message" to itemToDelete.message,
                                "isRead" to itemToDelete.isRead,
                                "createdAt" to Date(),
                                "badgeText" to itemToDelete.badgeText,
                                "requestId" to itemToDelete.requestId
                            )
                            firestore.collection("notifications")
                                .document(itemToDelete.id)
                                .set(notifMap)
                                .await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                snackbar.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        // Nếu không hoàn tác -> Xóa vĩnh viễn khỏi Firebase
                        if (event != DISMISS_EVENT_ACTION) {
                            lifecycleScope.launch {
                                try {
                                    firestore.collection("notifications")
                                        .document(itemToDelete.id)
                                        .delete()
                                        .await()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                })
                
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

        val userId = auth.currentUser?.uid
        if (userId == null) {
            progressBar.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "Vui lòng đăng nhập"
            return
        }

        lifecycleScope.launch {
            try {
                // Lấy tất cả notifications của user, sort trong bộ nhớ để tránh cần index
                val snapshot = firestore.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                allAlerts.clear()
                
                // Chuyển thành list tạm để sort
                val tempList = mutableListOf<AlertData>()
                
                snapshot.documents.forEach { doc ->
                    try {
                        val type = when (doc.getString("type")) {
                            "REQUEST_UPDATE" -> AlertType.REQUEST_UPDATE
                            "REQUEST_APPROVED" -> AlertType.REQUEST_APPROVED
                            "SLA_WARNING" -> AlertType.SLA_WARNING
                            "CHAT_MESSAGE" -> AlertType.CHAT_MESSAGE
                            "REQUEST_REJECTED" -> AlertType.REQUEST_REJECTED
                            else -> AlertType.INFO
                        }
                        
                        val createdAt = doc.getDate("createdAt")

                        val alert = AlertData(
                            id = doc.id,
                            type = type,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            time = formatTimeAgo(createdAt),
                            isRead = doc.getBoolean("isRead") ?: false,
                            group = getGroupFromDate(createdAt),
                            badgeText = doc.getString("badgeText"),
                            requestId = doc.getString("requestId"),
                            timestamp = createdAt?.time ?: 0L
                        )
                        tempList.add(alert)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Sort theo timestamp descending trong bộ nhớ
                allAlerts.addAll(tempList.sortedByDescending { it.timestamp })

                progressBar.visibility = View.GONE
                rvAlerts.visibility = View.VISIBLE

                filterData(0)
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmptyState.visibility = View.VISIBLE
                tvEmptyState.text = "Lỗi: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun formatTimeAgo(date: Date?): String {
        if (date == null) return ""

        val now = Date()
        val diff = now.time - date.time

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            minutes < 1 -> "Vừa xong"
            minutes < 60 -> "${minutes} phút trước"
            hours < 24 -> "${hours} giờ trước"
            days < 7 -> "${days} ngày trước"
            else -> "${days / 7} tuần trước"
        }
    }

    private fun getGroupFromDate(date: Date?): String {
        if (date == null) return "Khác"

        val now = Date()
        val diff = now.time - date.time
        val days = diff / (1000 * 60 * 60 * 24)

        return when {
            days < 1 -> "Hôm nay"
            days < 2 -> "Hôm qua"
            days < 7 -> "Tuần này"
            else -> "Trước đó"
        }
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
}