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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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
    private lateinit var firestore: FirebaseFirestore
    
    // Google Sign-In
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        // Initialize Facebook Login
        callbackManager = CallbackManager.Factory.create()
        
        // Initialize Google Sign-In
        initializeGoogleSignIn()

        // Kiểm tra nếu user đã đăng nhập (chỉ khi không phải từ logout)
        // Kiểm tra cả Firebase và Facebook
        val currentUser = auth.currentUser
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = (currentUser != null) || (accessToken != null && !accessToken.isExpired)
        
        if (isLoggedIn && !isComingFromLogout()) {
            navigateToHome()
            return
        }

        initViews()
        setupLoginButton()
        setupSocialLoginButtons()
        setupForgotPassword()
        setupRegisterButton()
        setupFacebookLogin()
    }
    
    private fun isComingFromLogout(): Boolean {
        // Kiểm tra intent flags để xem có phải từ logout không
        return (intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK) != 0
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
                    
                    // Thông báo lỗi dễ hiểu
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record", ignoreCase = true) == true ||
                        task.exception?.message?.contains("user may have been deleted", ignoreCase = true) == true ->
                            "Tài khoản không tồn tại. Vui lòng đăng ký trước."
                        
                        task.exception?.message?.contains("password is invalid", ignoreCase = true) == true ||
                        task.exception?.message?.contains("credential is incorrect", ignoreCase = true) == true ||
                        task.exception?.message?.contains("malformed", ignoreCase = true) == true ->
                            "Email hoặc mật khẩu không đúng. Vui lòng thử lại."
                        
                        task.exception?.message?.contains("too many requests", ignoreCase = true) == true ->
                            "Quá nhiều lần thử. Vui lòng thử lại sau."
                        
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Lỗi kết nối mạng. Vui lòng kiểm tra Internet."
                        
                        else -> "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin."
                    }
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setupSocialLoginButtons() {
        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
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
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
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
    
    private fun initializeGoogleSignIn() {
        // Cấu hình Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Facebook callback
        callbackManager.onActivityResult(requestCode, resultCode, data)
        
        // Google Sign-In callback
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }
    
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Google sign in success: ${account.email}")
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed", e)
            
            val errorMessage = when (e.statusCode) {
                12501 -> "Đăng nhập bị hủy"
                12500 -> "Lỗi cấu hình Google Sign-In. Vui lòng kiểm tra google-services.json"
                7 -> "Lỗi kết nối mạng. Vui lòng thử lại."
                else -> "Đăng nhập Google thất bại: ${e.message}"
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val firebaseUser = auth.currentUser
                    Log.d(TAG, "User: ${firebaseUser?.displayName}, Email: ${firebaseUser?.email}")
                    
                    if (firebaseUser != null) {
                        // Save or update Google user in Firestore
                        saveGoogleUserToFirestore(
                            firebaseUser.uid,
                            firebaseUser.displayName ?: "Google User",
                            firebaseUser.email ?: "",
                            firebaseUser.photoUrl?.toString()
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
                        "Xác thực Google thất bại: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
    private fun saveGoogleUserToFirestore(
        userId: String, 
        displayName: String, 
        email: String,
        photoUrl: String?
    ) {
        // Check if user already exists
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User already exists, just navigate
                    Log.d(TAG, "Google user already exists in Firestore")
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
                        "avatar" to photoUrl,
                        "birthDate" to null,
                        "departmentId" to null,
                        "departmentName" to null,
                        "emailConfirmed" to true, // Google email is verified
                        "phoneNumberConfirmed" to false,
                        "roles" to listOf("User"),
                        "createdAt" to Date()
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Google user saved to Firestore")
                            Toast.makeText(
                                this,
                                "Chào mừng ${displayName}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToHome()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error saving Google user to Firestore", e)
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
                    val firebaseUser = auth.currentUser
                    Log.d(TAG, "User: ${firebaseUser?.displayName}, Email: ${firebaseUser?.email}")
                    
                    if (firebaseUser != null) {
                        // Save or update Facebook user in Firestore
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
    
}