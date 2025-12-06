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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.irequest.data.models.Department
import com.example.irequest.data.models.Employee
import com.project.irequest.ui.DepartmentAdapter

class DepartmentActivity : AppCompatActivity() {

    private lateinit var rvDepartments: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var etSearch: EditText

    private val adapter = DepartmentAdapter(emptyList())

    // 👇 Biến dùng để tạo độ trễ khi gõ phím (Sửa lỗi mất chữ tiếng Việt)
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_department)

        initViews()
        setupSearch()
        loadMockData()
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

    private fun loadMockData() {
        progressBar.visibility = View.VISIBLE

        // Dữ liệu giả
        val fakeList = listOf(
            Department(
                departmentId = 1,
                name = "Ban Giám Đốc",
                assignedUserName = "Trần Văn CEO",
                description = "Điều hành toàn bộ hoạt động công ty",
                employees = listOf(
                    Employee("Trần Văn CEO", "Tổng Giám Đốc"),
                    Employee("Lê Thư Ký", "Thư ký TGĐ")
                )
            ),
            Department(
                departmentId = 2,
                name = "Phòng IT",
                assignedUserName = "Lê Văn Code",
                description = "Hỗ trợ kỹ thuật và phần mềm",
                employees = listOf(
                    Employee("Lê Văn Code", "Trưởng phòng"),
                    Employee("Nguyễn Fullstack", "Senior Dev"),
                    Employee("Trần Mobile", "Android Dev")
                )
            ),
            Department(
                departmentId = 3,
                name = "Phòng Kế Toán",
                assignedUserName = "Phạm Thị Tiền",
                description = "Quản lý tài chính và lương thưởng",
                employees = listOf(
                    Employee("Phạm Thị Tiền", "Kế toán trưởng"),
                    Employee("Vũ Thu Chi", "Thủ quỹ")
                )
            ),
            Department(
                departmentId = 4,
                name = "Phòng Nhân Sự",
                assignedUserName = "Nguyễn Thị Mai",
                description = "Tuyển dụng và chế độ phúc lợi",
                employees = listOf(
                    Employee("Nguyễn Thị Mai", "Trưởng phòng HR"),
                    Employee("Trần Tuyển Dụng", "Chuyên viên")
                )
            )
        )

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE
            adapter.updateData(fakeList)
        }, 500)
    }
}