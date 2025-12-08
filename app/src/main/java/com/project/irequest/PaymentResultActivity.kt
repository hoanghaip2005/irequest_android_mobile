package com.project.irequest

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.irequest.data.models.PaymentHistory
import com.example.irequest.data.repository.FirebasePaymentRepository
import kotlinx.coroutines.launch

class PaymentResultActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val repository = FirebasePaymentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_result)

        webView = findViewById(R.id.webView)
        setupWebView()

        // Lấy URL từ intent
        val resultUrl = intent.getStringExtra("RESULT_URL")
        if (resultUrl != null) {
            webView.loadUrl(resultUrl)
            
            // Parse và lưu vào Firebase
            parseAndSavePayment(resultUrl)
        } else {
            Toast.makeText(this, "Không nhận được kết quả thanh toán", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Nếu user click về app, đóng activity này
                if (url?.startsWith("irequest://") == true) {
                    finish()
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Thêm nút đóng bằng JavaScript
                webView.evaluateJavascript("""
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = '.close-btn { position: fixed; top: 20px; right: 20px; background: #fff; border: 2px solid #333; border-radius: 50%; width: 40px; height: 40px; font-size: 24px; cursor: pointer; z-index: 9999; }';
                        document.head.appendChild(style);
                        
                        var btn = document.createElement('button');
                        btn.className = 'close-btn';
                        btn.innerHTML = '×';
                        btn.onclick = function() { window.location.href = 'irequest://close'; };
                        document.body.appendChild(btn);
                    })();
                """, null)
            }
        }
    }

    private fun parseAndSavePayment(url: String) {
        try {
            val uri = android.net.Uri.parse(url)
            val orderId = uri.getQueryParameter("orderId") ?: return
            val resultCode = uri.getQueryParameter("resultCode")
            val transId = uri.getQueryParameter("transId") ?: ""
            val amount = uri.getQueryParameter("amount")?.toLongOrNull() ?: 0
            val orderInfo = uri.getQueryParameter("orderInfo") ?: ""
            val payType = uri.getQueryParameter("payType") ?: ""

            // Lấy thông tin từ SharedPreferences
            val prefs = getSharedPreferences("payment_pending", MODE_PRIVATE)
            val paymentName = prefs.getString("paymentName", orderInfo) ?: orderInfo
            val description = prefs.getString("description", "") ?: ""

            val status = if (resultCode == "0") "success" else "failed"

            lifecycleScope.launch {
                try {
                    val payment = PaymentHistory(
                        orderId = orderId,
                        paymentName = paymentName,
                        amount = amount,
                        description = description,
                        status = status,
                        transId = transId,
                        payType = payType
                    )

                    repository.savePayment(payment).onSuccess {
                        android.util.Log.d("PaymentResult", "Saved payment: $orderId")
                        
                        // Xóa pending
                        prefs.edit().clear().apply()
                    }.onFailure { e ->
                        android.util.Log.e("PaymentResult", "Failed to save: ${e.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PaymentResult", "Error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PaymentResult", "Parse error: ${e.message}")
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
