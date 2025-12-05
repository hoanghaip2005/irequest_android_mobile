package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: LinearLayout
    private lateinit var btnFacebookLogin: LinearLayout
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegister: TextView
    
    // Facebook Login
    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Kiểm tra nếu user đã đăng nhập bằng Facebook
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn) {
            navigateToHome()
            return
        }
        
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()

        initViews()
        setupLoginButton()
        setupSocialLoginButtons()
        setupForgotPassword()
        setupRegisterButton()
        setupFacebookLogin()
    }

    private fun initViews() {
        etUsername = findViewById<EditText>(R.id.etUsername)
        etPassword = findViewById<EditText>(R.id.etPassword)
        btnLogin = findViewById<Button>(R.id.btnLogin)
        btnGoogleLogin = findViewById<LinearLayout>(R.id.btnGoogleLogin)
        btnFacebookLogin = findViewById<LinearLayout>(R.id.btnFacebookLogin)
        tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        tvRegister = findViewById<TextView>(R.id.tvRegister)
    }

    private fun setupLoginButton() {
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Demo login (xóa dòng này khi có Firebase thật)
            if (email == "admin" && password == "123456") {
                navigateToHome()
                return@setOnClickListener
            }

            // Firebase Authentication login
            loginWithEmailPassword(email, password)
        }
    }
    
    private fun loginWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    Toast.makeText(
                        this,
                        "Đăng nhập thành công: ${user?.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Đăng nhập thất bại: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
            // Sử dụng LoginManager để đăng nhập với Facebook
            // Từ Facebook SDK 17.x+, không cần ActivityResultLauncher
            // SDK tự động xử lý với Activity Result API
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("public_profile", "email")
            )
        }
    }

    private fun setupForgotPassword() {
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng quên mật khẩu", Toast.LENGTH_SHORT).show()
            // TODO: Implement forgot password functionality
        }
    }
    
    private fun setupRegisterButton() {
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun setupFacebookLogin() {
        // Đăng ký callback cho Facebook login
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "Facebook login success")
                    // Đăng nhập thành công, lấy access token
                    handleFacebookAccessToken(loginResult.accessToken.token)
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled")
                    Toast.makeText(
                        this@LoginActivity,
                        "Đăng nhập bị hủy",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login error", error)
                    
                    val errorMessage = when {
                        error.message?.contains("OAuth", ignoreCase = true) == true -> 
                            "Ứng dụng Facebook chưa được cấu hình đúng. Vui lòng kiểm tra Facebook Developer Console."
                        error.message?.contains("network", ignoreCase = true) == true ->
                            "Lỗi kết nối mạng. Vui lòng thử lại."
                        else -> "Lỗi đăng nhập: ${error.message}"
                    }
                    
                    Toast.makeText(
                        this@LoginActivity,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    
    private fun handleFacebookAccessToken(token: String) {
        Log.d(TAG, "Handling Facebook access token...")
        
        // Tích hợp với Firebase Authentication
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Log.d(TAG, "User: ${user?.displayName}, Email: ${user?.email}")
                    Toast.makeText(
                        this,
                        "Chào mừng ${user?.displayName ?: "bạn"}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Chuyển sang HomeActivity
                    navigateToHome()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Xác thực thất bại: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
}