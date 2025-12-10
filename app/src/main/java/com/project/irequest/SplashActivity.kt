package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.irequest.utils.SessionManager

/**
 * SplashActivity - Màn hình khởi động
 * Hiển thị logo và kiểm tra trạng thái để điều hướng
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var auth: FirebaseAuth
    
    companion object {
        private const val SPLASH_DELAY = 2000L // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Don't call setContentView - use theme background instead
        
        sessionManager = SessionManager(this)
        auth = FirebaseAuth.getInstance()
        
        // Delay then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToAppropriateScreen()
        }, SPLASH_DELAY)
    }

    private fun navigateToAppropriateScreen() {
        val intent = when {
            // Chưa xem onboarding -> Onboarding
            !sessionManager.isOnboardingCompleted() -> {
                Intent(this, OnboardingActivity::class.java)
            }
            // Đã login -> Home
            auth.currentUser != null && sessionManager.isLoggedIn() -> {
                Intent(this, HomeActivity::class.java)
            }
            // Chưa login -> Login
            else -> {
                Intent(this, LoginActivity::class.java)
            }
        }
        
        startActivity(intent)
        finish()
    }
}
