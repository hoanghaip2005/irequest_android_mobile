package com.project.irequest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth

class AccountActivity : BaseActivity() {
    private lateinit var buttonLogout: AppCompatButton
    private lateinit var itemPersonalInfo: LinearLayout
    private lateinit var itemSecurityPrivacy: LinearLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()

        initViews()
        setupClickListeners()
        
        setupBottomNavigation()
        setActiveTab(3)
    }

    private fun initViews() {
        buttonLogout = findViewById(R.id.button_logout)
        itemPersonalInfo = findViewById(R.id.item_personal_info)
        itemSecurityPrivacy = findViewById(R.id.item_security_privacy)
    }

    private fun setupClickListeners() {
        buttonLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        itemPersonalInfo.setOnClickListener {
            val intent = Intent(this, PersonalInformationActivity::class.java)
            startActivity(intent)
        }

        itemSecurityPrivacy.setOnClickListener {
            val intent = Intent(this, SecurityPrivacyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout_confirmation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)
        val btnLogout = dialog.findViewById<Button>(R.id.btn_logout)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnLogout.setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        auth.signOut()
        LoginManager.getInstance().logOut()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Override navigation methods
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
        // Already here
    }
}
