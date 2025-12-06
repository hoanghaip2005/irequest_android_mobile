package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {
    // Views
    private lateinit var btnBack: ImageView
    private lateinit var tvDescription: TextView
    private lateinit var layoutEmail: LinearLayout
    private lateinit var layoutSuccess: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var tvSuccessMessage: TextView
    private lateinit var tvResendEmail: TextView
    private lateinit var btnPrimary: Button
    private lateinit var tvBackToLogin: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth

    // State
    private var currentEmail: String = ""

    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvDescription = findViewById(R.id.tvDescription)
        layoutEmail = findViewById(R.id.layoutEmail)
        layoutSuccess = findViewById(R.id.layoutSuccess)
        etEmail = findViewById(R.id.etEmail)
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage)
        tvResendEmail = findViewById(R.id.tvResendEmail)
        btnPrimary = findViewById(R.id.btnPrimary)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnPrimary.setOnClickListener {
            sendPasswordResetEmail()
        }

        tvResendEmail.setOnClickListener {
            sendPasswordResetEmail()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = if (currentEmail.isNotEmpty()) {
            currentEmail
        } else {
            etEmail.text.toString().trim()
        }

        // Validate email
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        currentEmail = email

        // Disable button to prevent spam
        btnPrimary.isEnabled = false
        btnPrimary.alpha = 0.5f

        // Gửi email reset password qua Firebase
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                btnPrimary.isEnabled = true
                btnPrimary.alpha = 1f

                if (task.isSuccessful) {
                    Log.d(TAG, "Password reset email sent to: $email")
                    showSuccessMessage(email)
                } else {
                    Log.w(TAG, "Failed to send reset email", task.exception)
                    
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record", ignoreCase = true) == true ->
                            "Email không tồn tại trong hệ thống"
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Lỗi kết nối mạng. Vui lòng thử lại"
                        task.exception?.message?.contains("too-many-requests", ignoreCase = true) == true ->
                            "Quá nhiều yêu cầu. Vui lòng thử lại sau"
                        else -> "Không thể gửi email. Vui lòng thử lại"
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showSuccessMessage(email: String) {
        // Hide email input, show success message
        layoutEmail.visibility = View.GONE
        layoutSuccess.visibility = View.VISIBLE
        btnPrimary.visibility = View.GONE

        // Update success message
        tvSuccessMessage.text = "Chúng tôi đã gửi liên kết đặt lại mật khẩu đến:\n\n$email\n\nVui lòng kiểm tra hộp thư đến (và cả thư spam) để đặt lại mật khẩu."

        Toast.makeText(
            this,
            "Email đã được gửi thành công!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
