package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class RegisterActivity : AppCompatActivity() {
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnGoogleRegister: LinearLayout
    private lateinit var btnFacebookRegister: LinearLayout
    private lateinit var tvLogin: TextView
    
    // Firebase & Facebook
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var callbackManager: CallbackManager

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()

        initViews()
        setupRegisterButton()
        setupSocialRegisterButtons()
        setupLoginLink()
        setupFacebookLogin()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogleRegister = findViewById(R.id.btnGoogleRegister)
        btnFacebookRegister = findViewById(R.id.btnFacebookRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupRegisterButton() {
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation
            if (fullName.isEmpty()) {
                etFullName.error = "Vui lòng nhập họ và tên"
                etFullName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Vui lòng nhập email"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Email không hợp lệ"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Vui lòng nhập mật khẩu"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                etConfirmPassword.error = "Vui lòng xác nhận mật khẩu"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Mật khẩu không khớp"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // Register with Firebase
            registerWithEmailPassword(fullName, email, password)
        }
    }

    private fun registerWithEmailPassword(fullName: String, email: String, password: String) {
        // Disable button during registration
        btnRegister.isEnabled = false
        btnRegister.text = "Đang đăng ký..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        // Update user profile with display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                        
                        firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Log.d(TAG, "User profile updated.")
                                    // Save additional user info to Firestore
                                    saveUserToFirestore(firebaseUser.uid, fullName, email)
                                } else {
                                    btnRegister.isEnabled = true
                                    btnRegister.text = "Đăng Ký"
                                    Toast.makeText(this, "Lỗi cập nhật profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        btnRegister.isEnabled = true
                        btnRegister.text = "Đăng Ký"
                    }
                } else {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Đăng Ký"
                    
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "Email này đã được sử dụng"
                        task.exception?.message?.contains("network") == true ->
                            "Lỗi kết nối mạng. Vui lòng thử lại."
                        else -> "Đăng ký thất bại: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, fullName: String, email: String) {
        val userData = hashMapOf(
            "id" to userId,
            "userName" to fullName,
            "email" to email,
            "phoneNumber" to null,
            "homeAddress" to null,
            "avatar" to null,
            "birthDate" to null,
            "departmentId" to null,
            "departmentName" to null,
            "emailConfirmed" to false,
            "phoneNumberConfirmed" to false,
            "roles" to listOf("User"),
            "createdAt" to Date()
        )

        firestore.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved to Firestore")
                btnRegister.isEnabled = true
                btnRegister.text = "Đăng Ký"
                
                Toast.makeText(
                    this,
                    "Đăng ký thành công! Chào mừng $fullName",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Navigate to Home
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving user to Firestore", e)
                btnRegister.isEnabled = true
                btnRegister.text = "Đăng Ký"
                
                Toast.makeText(
                    this,
                    "Đăng ký thành công nhưng lỗi lưu thông tin. Vui lòng đăng nhập.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Still navigate to home as auth was successful
                navigateToHome()
            }
    }

    private fun setupSocialRegisterButtons() {
        btnGoogleRegister.setOnClickListener {
            Toast.makeText(this, "Đăng ký với Google", Toast.LENGTH_SHORT).show()
            // TODO: Implement Google registration
        }

        btnFacebookRegister.setOnClickListener {
            // Sử dụng LoginManager để đăng ký/đăng nhập với Facebook
            LoginManager.getInstance().logInWithReadPermissions(
                this,
                listOf("public_profile", "email")
            )
        }
    }

    private fun setupFacebookLogin() {
        // Đăng ký callback cho Facebook login
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "Facebook login success")
                    handleFacebookAccessToken(loginResult.accessToken.token)
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled")
                    Toast.makeText(
                        this@RegisterActivity,
                        "Đăng ký bị hủy",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login error", error)
                    val errorMessage = when {
                        error.message?.contains("OAuth", ignoreCase = true) == true ->
                            "Ứng dụng Facebook chưa được cấu hình đúng."
                        error.message?.contains("network", ignoreCase = true) == true ->
                            "Lỗi kết nối mạng. Vui lòng thử lại."
                        else -> "Lỗi đăng ký: ${error.message}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun handleFacebookAccessToken(token: String) {
        Log.d(TAG, "Handling Facebook access token...")

        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val firebaseUser = auth.currentUser
                    
                    if (firebaseUser != null) {
                        // Save Facebook user to Firestore
                        saveFacebookUserToFirestore(
                            firebaseUser.uid,
                            firebaseUser.displayName ?: "Facebook User",
                            firebaseUser.email ?: ""
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Chào mừng bạn!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateToHome()
                    }
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

    private fun saveFacebookUserToFirestore(userId: String, displayName: String, email: String) {
        // Check if user already exists
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User already exists, just navigate
                    Log.d(TAG, "Facebook user already exists in Firestore")
                    Toast.makeText(
                        this,
                        "Chào mừng ${displayName}!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome()
                } else {
                    // Create new user document
                    val userData = hashMapOf(
                        "id" to userId,
                        "userName" to displayName,
                        "email" to email,
                        "phoneNumber" to null,
                        "homeAddress" to null,
                        "avatar" to null,
                        "birthDate" to null,
                        "departmentId" to null,
                        "departmentName" to null,
                        "emailConfirmed" to true, // Facebook email is verified
                        "phoneNumberConfirmed" to false,
                        "roles" to listOf("User"),
                        "createdAt" to Date()
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Facebook user saved to Firestore")
                            Toast.makeText(
                                this,
                                "Chào mừng ${displayName}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToHome()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error saving Facebook user to Firestore", e)
                            Toast.makeText(
                                this,
                                "Chào mừng ${displayName}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToHome()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking user in Firestore", e)
                Toast.makeText(
                    this,
                    "Chào mừng ${displayName}!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToHome()
            }
    }

    private fun setupLoginLink() {
        tvLogin.setOnClickListener {
            finish() // Quay lại LoginActivity
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
