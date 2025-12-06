package com.project.irequest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : BaseActivity() {
    private lateinit var buttonLogout: AppCompatButton
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        initViews()
        setupLogoutButton()
        
        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        
        // Set tab Account là active (index 3)
        setActiveTab(3)
    }

    private fun initViews() {
        buttonLogout = findViewById(R.id.button_logout)
    }

    private fun setupLogoutButton() {
        buttonLogout.setOnClickListener {
            // Đăng xuất khỏi Firebase
            auth.signOut()
            
            // Đăng xuất khỏi Facebook
            LoginManager.getInstance().logOut()
            
            // Chuyển về LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Override navigation methods để xử lý riêng cho AccountActivity
    override fun onNavigationHomeClicked() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNavigationWorkClicked() {
        val intent = Intent(this, WorkActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNavigationChatClicked() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNavigationAccountClicked() {
        // Already in AccountActivity, do nothing
    }
}
