package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View // Thêm import này
import android.widget.EditText
import android.widget.TextView // Dùng TextView thay vì Button để tránh lỗi
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    // 👇 SỬA Ở ĐÂY: Đổi hết từ Button thành View (hoặc TextView)
    private lateinit var btnLogin: View
    private lateinit var btnGoogleLogin: View
    private lateinit var btnFacebookLogin: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupLoginButton()
        setupSocialLoginButtons()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        // 👇 Các dòng này sẽ không bị lỗi ép kiểu nữa vì View là cha của tất cả
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin)
    }

    private fun setupLoginButton() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple validation
            if (username == "admin" && password == "123456") {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSocialLoginButtons() {
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Đăng nhập với Google", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }

        btnFacebookLogin.setOnClickListener {
            Toast.makeText(this, "Đăng nhập với Facebook", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        // Lưu ý: Đảm bảo bạn ĐÃ CÓ file HomeActivity.kt nhé
        // Nếu chưa có HomeActivity, dòng này sẽ báo đỏ.
        // Tạm thời comment lại nếu chưa tạo HomeActivity.
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}