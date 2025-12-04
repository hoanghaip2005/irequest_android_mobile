package com.project.irequest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.NumberFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageView
    private lateinit var etPaymentName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPayment: Button
    
    private val client = OkHttpClient()
    private val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        initViews()
        setupListeners()
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etPaymentName = findViewById(R.id.etPaymentName)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPayment = findViewById(R.id.btnPayment)
    }
    
    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        // Cập nhật tổng tiền khi người dùng nhập
        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val amountStr = s.toString()
                if (amountStr.isNotEmpty()) {
                    try {
                        val amount = amountStr.toLong()
                        tvTotalAmount.text = "${numberFormat.format(amount)} VNĐ"
                    } catch (e: NumberFormatException) {
                        tvTotalAmount.text = "0 VNĐ"
                    }
                } else {
                    tvTotalAmount.text = "0 VNĐ"
                }
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        btnPayment.setOnClickListener {
            processPayment()
        }
    }
    
    private fun processPayment() {
        val paymentName = etPaymentName.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        
        // Validate input
        if (paymentName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên nộp phí", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = try {
            amountStr.toLong()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amount < 1000) {
            Toast.makeText(this, "Số tiền phải lớn hơn hoặc bằng 1,000 VNĐ", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Gọi API để tạo thanh toán
        createMoMoPayment(paymentName, amount, description)
    }
    
    private fun createMoMoPayment(paymentName: String, amount: Long, description: String) {
        // Hiển thị loading
        btnPayment.isEnabled = false
        btnPayment.text = "Đang xử lý..."
        
        // Tạo JSON request body
        val jsonObject = JSONObject().apply {
            put("paymentName", paymentName)
            put("amount", amount)
            put("description", description)
        }
        
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)
        
        // Gửi request đến server
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/api/create") // 10.0.2.2 là localhost cho Android emulator
            .post(requestBody)
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    btnPayment.isEnabled = true
                    btnPayment.text = "Thanh Toán"
                    Toast.makeText(
                        this@PaymentActivity,
                        "Lỗi kết nối: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                runOnUiThread {
                    btnPayment.isEnabled = true
                    btnPayment.text = "Thanh Toán"
                    
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val data = jsonResponse.getJSONObject("data")
                            val payUrl = data.getString("payUrl")
                            
                            // Mở trình duyệt với URL thanh toán
                            openPaymentUrl(payUrl)
                            
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@PaymentActivity,
                                "Lỗi xử lý dữ liệu: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@PaymentActivity,
                            "Lỗi tạo thanh toán: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
    
    private fun openPaymentUrl(payUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
            startActivity(intent)
            
            // Có thể finish() activity này sau khi mở trình duyệt
            // finish()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Không thể mở trình duyệt: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
