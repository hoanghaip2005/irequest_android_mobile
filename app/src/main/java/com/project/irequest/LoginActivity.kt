package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnFacebookLogin: LinearLayout
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupLoginButton()
        setupSocialLoginButtons()
        setupForgotPassword()
    }

    private fun initViews() {
        etUsername = findViewById<EditText>(R.id.etUsername)
        etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnGoogleLogin = findViewById<LinearLayout>(R.id.btnGoogleLogin)
        btnFacebookLogin = findViewById<LinearLayout>(R.id.btnFacebookLogin)
        tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
    }

    private fun setupLoginButton() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple validation - you can replace this with actual authentication
            if (username == "admin" && password == "123456") {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Close login activity
            } else {
                Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSocialLoginButtons() {
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Đăng nhập với Google", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google login
            // For demo, we'll just navigate to home
            navigateToHome()
        }

        btnFacebookLogin.setOnClickListener {
            Toast.makeText(this, "Đăng nhập với Facebook", Toast.LENGTH_SHORT).show()
            // TODO: Implement Facebook login  
            // For demo, we'll just navigate to home
            navigateToHome()
        }
    }

    private fun setupForgotPassword() {
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng quên mật khẩu", Toast.LENGTH_SHORT).show()
            // TODO: Implement forgot password functionality
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}