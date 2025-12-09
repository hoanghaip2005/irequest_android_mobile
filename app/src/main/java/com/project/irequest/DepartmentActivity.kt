package com.project.irequest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Department
import com.example.irequest.data.models.Employee
import com.example.irequest.data.repository.FirebaseDepartmentRepository
import com.project.irequest.ui.DepartmentAdapter
import kotlinx.coroutines.launch

class DepartmentActivity : AppCompatActivity() {

    private lateinit var rvDepartments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText

    private val adapter = DepartmentAdapter(emptyList())
    private val departmentRepository = FirebaseDepartmentRepository()

    // 👇 Biến dùng để tạo độ trễ khi gõ phím (Sửa lỗi mất chữ tiếng Việt)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department)

        initViews()
        setupSearch()
        loadDepartmentsFromFirebase()
    }

    private fun initViews() {
        rvDepartments = findViewById(R.id.rvDepartments)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)

        rvDepartments.layoutManager = LinearLayoutManager(this)
        rvDepartments.adapter = adapter
        btnBack.setOnClickListener { finish() }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Hủy lệnh tìm kiếm cũ nếu người dùng vẫn đang gõ tiếp
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
            }

            override fun afterTextChanged(s: Editable?) {
                // CHỜ 300ms SAU KHI NGỪNG GÕ MỚI BẮT ĐẦU TÌM
                // Cách này giúp gõ tiếng Việt không bị lỗi mất dấu
                searchRunnable = Runnable {
                    val query = s.toString().trim()
                    adapter.filter(query)
                }
                searchHandler.postDelayed(searchRunnable!!, 300)
            }
        })
    }

    private fun loadDepartmentsFromFirebase() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            departmentRepository.getAllDepartments()
                .onSuccess { departments ->
                    progressBar.visibility = View.GONE
                    
                    if (departments.isEmpty()) {
                        Toast.makeText(
                            this@DepartmentActivity,
                            "Chưa có phòng ban nào. Vui lòng chạy script firebase_department_setup.py",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Fallback to mock data if Firebase is empty
                        loadMockDataFallback()
                    } else {
                        adapter.updateData(departments)
                        Toast.makeText(
                            this@DepartmentActivity,
                            "Đã tải ${departments.size} phòng ban",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .onFailure { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@DepartmentActivity,
                        "Lỗi kết nối Firebase: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Fallback to mock data on error
                    loadMockDataFallback()
                }
        }
    }
    
    private fun loadMockDataFallback() {
        // Dữ liệu giả (fallback khi Firebase chưa có dữ liệu)
        val fakeList = listOf(
            Department(
                departmentId = 1,
                name = "Ban Giám Đốc",
                assignedUserName = "Trần Văn CEO",
                description = "Điều hành toàn bộ hoạt động công ty",
                employees = listOf(
                    Employee(name = "Trần Văn CEO", role = "Tổng Giám Đốc", department = "Ban Giám Đốc"),
                    Employee(name = "Lê Thư Ký", role = "Thư ký TGĐ", department = "Ban Giám Đốc")
                )
            ),
            Department(
                departmentId = 2,
                name = "Phòng IT",
                assignedUserName = "Lê Văn Code",
                description = "Hỗ trợ kỹ thuật và phần mềm",
                employees = listOf(
                    Employee(name = "Lê Văn Code", role = "Trưởng phòng", department = "Phòng IT"),
                    Employee(name = "Nguyễn Fullstack", role = "Senior Dev", department = "Phòng IT"),
                    Employee(name = "Trần Mobile", role = "Android Dev", department = "Phòng IT")
                )
            ),
            Department(
                departmentId = 3,
                name = "Phòng Kế Toán",
                assignedUserName = "Phạm Thị Tiền",
                description = "Quản lý tài chính và lương thưởng",
                employees = listOf(
                    Employee(name = "Phạm Thị Tiền", role = "Kế toán trưởng", department = "Phòng Kế Toán"),
                    Employee(name = "Vũ Thu Chi", role = "Thủ quỹ", department = "Phòng Kế Toán")
                )
            ),
            Department(
                departmentId = 4,
                name = "Phòng Nhân Sự",
                assignedUserName = "Nguyễn Thị Mai",
                description = "Tuyển dụng và chế độ phúc lợi",
                employees = listOf(
                    Employee(name = "Nguyễn Thị Mai", role = "Trưởng phòng HR", department = "Phòng Nhân Sự"),
                    Employee(name = "Trần Tuyển Dụng", role = "Chuyên viên", department = "Phòng Nhân Sự")
                )
            )
        )

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE
            adapter.updateData(fakeList)
        }, 500)
    }
}