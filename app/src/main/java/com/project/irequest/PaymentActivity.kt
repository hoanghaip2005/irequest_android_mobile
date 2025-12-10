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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PaymentActivity : AppCompatActivity() {
    
    private lateinit var btnBack: ImageView
    private lateinit var etPaymentName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPayment: Button
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val numberFormat = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        initViews()
        setupListeners()
        
        // Xử lý deep link callback từ MoMo
        handleDeepLink(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Xử lý khi activity đã tồn tại (launchMode=singleTop)
        handleDeepLink(intent)
    }
    
    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etPaymentName = findViewById(R.id.etPaymentName)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPayment = findViewById(R.id.btnPayment)
        
        // Nút lịch sử
        findViewById<android.widget.Button>(R.id.btnHistory).setOnClickListener {
            startActivity(android.content.Intent(this, PaymentHistoryActivity::class.java))
        }
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
        btnPayment.isEnabled = false
        btnPayment.text = "Đang xử lý..."
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val jsonObject = JSONObject().apply {
                        put("paymentName", paymentName)
                        put("amount", amount)
                        put("description", description)
                    }
                    
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val requestBody = jsonObject.toString().toRequestBody(mediaType)
                    
                    val request = Request.Builder()
                        .url("http://10.0.2.2:3000/api/create") // 10.0.2.2 = localhost của máy host khi dùng emulator
                        .post(requestBody)
                        .build()
                    
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful && responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        jsonResponse.getJSONObject("data")
                    } else {
                        throw Exception("Lỗi kết nối: ${response.code}")
                    }
                }
                
                // Lấy thông tin thanh toán
                val orderId = result.getString("orderId")
                val payUrl = result.optString("payUrl")
                val deeplink = result.optString("deeplink")
                
                android.util.Log.d("PaymentActivity", "OrderId: $orderId")
                android.util.Log.d("PaymentActivity", "PayUrl: $payUrl")
                android.util.Log.d("PaymentActivity", "Deeplink: $deeplink")
                
                if (payUrl.isEmpty() && deeplink.isEmpty()) {
                    Toast.makeText(this@PaymentActivity, "Không nhận được link thanh toán", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Lưu thông tin pending để xử lý sau
                savePaymentPending(orderId, paymentName, amount, description)
                
                // Thử mở MoMo app qua deeplink trước
                var opened = false
                if (deeplink.isNotEmpty()) {
                    try {
                        val deeplinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplink))
                        deeplinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        
                        if (deeplinkIntent.resolveActivity(packageManager) != null) {
                            startActivity(deeplinkIntent)
                            Toast.makeText(this@PaymentActivity, "Đang mở ứng dụng MoMo...", Toast.LENGTH_SHORT).show()
                            opened = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PaymentActivity", "Deeplink failed: ${e.message}")
                    }
                }
                
                // Nếu deeplink không mở được, dùng payUrl (web)
                if (!opened && payUrl.isNotEmpty()) {
                    try {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(webIntent)
                        Toast.makeText(this@PaymentActivity, "Đang mở trình duyệt...", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@PaymentActivity, "Không thể mở thanh toán: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@PaymentActivity,
                    "Lỗi: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnPayment.isEnabled = true
                btnPayment.text = "Thanh Toán"
            }
        }
    }
    
    private fun savePaymentPending(orderId: String, paymentName: String, amount: Long, description: String) {
        val prefs = getSharedPreferences("payment_pending", MODE_PRIVATE)
        prefs.edit().apply {
            putString("orderId", orderId)
            putString("paymentName", paymentName)
            putLong("amount", amount)
            putString("description", description)
            putLong("timestamp", System.currentTimeMillis())
            apply()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Deep link sẽ xử lý callback tự động, không cần check pending nữa
    }
    
    private fun handleDeepLink(intent: android.content.Intent?) {
        val data = intent?.data
        
        if (data != null && data.scheme == "irequest" && data.host == "payment") {
            android.util.Log.d("PaymentActivity", "Deep link received: $data")
            
            val path = data.path // "/success" hoặc "/failed"
            val orderId = data.getQueryParameter("orderId")
            val transId = data.getQueryParameter("transId")
            val amount = data.getQueryParameter("amount")?.toLongOrNull() ?: 0
            val payType = data.getQueryParameter("payType") ?: ""
            val resultCode = data.getQueryParameter("resultCode")
            
            android.util.Log.d("PaymentActivity", "Path: $path, OrderId: $orderId")
            
            if (path == "/success" && orderId != null) {
                // Thanh toán thành công
                savePaymentHistoryFromCallback(
                    orderId = orderId,
                    transId = transId ?: "",
                    amount = amount,
                    payType = payType,
                    status = "success"
                )
                
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show()
            } else if (path == "/failed" && orderId != null) {
                // Thanh toán thất bại
                Toast.makeText(
                    this,
                    "Thanh toán thất bại! Mã lỗi: $resultCode",
                    Toast.LENGTH_LONG
                ).show()
                
                // Xóa pending payment
                getSharedPreferences("payment_pending", MODE_PRIVATE).edit().clear().apply()
            }
        }
    }
    
    private fun savePaymentHistoryFromCallback(
        orderId: String,
        transId: String,
        amount: Long,
        payType: String,
        status: String
    ) {
        lifecycleScope.launch {
            try {
                val prefs = getSharedPreferences("payment_pending", MODE_PRIVATE)
                val paymentName = prefs.getString("paymentName", "Thanh toán") ?: "Thanh toán"
                val description = prefs.getString("description", "") ?: ""
                
                val payment = com.example.irequest.data.models.PaymentHistory(
                    orderId = orderId,
                    paymentName = paymentName,
                    amount = amount,
                    description = description,
                    status = status,
                    transId = transId,
                    payType = payType
                )
                
                val repository = com.example.irequest.data.repository.FirebasePaymentRepository()
                repository.savePayment(payment).onSuccess {
                    android.util.Log.d("PaymentActivity", "Payment saved: $orderId")
                    
                    // Xóa pending
                    prefs.edit().clear().apply()
                }.onFailure { e ->
                    android.util.Log.e("PaymentActivity", "Save failed: ${e.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("PaymentActivity", "Error: ${e.message}")
            }
        }
    }
}