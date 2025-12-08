package com.project.irequest

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SecurityPrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_security_privacy)

        val ivBack = findViewById<ImageView>(R.id.iv_back)
        ivBack.setOnClickListener {
            onBackPressed()
        }

        val btnCancel = findViewById<Button>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            finish()
        }

        val btnSave = findViewById<Button>(R.id.btn_save)
        btnSave.setOnClickListener {
            // TODO: Implement save logic
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        }

        val switch2fa = findViewById<SwitchMaterial>(R.id.switch_2fa)
        switch2fa.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "2FA is " + (if (isChecked) "on" else "off"), Toast.LENGTH_SHORT).show()
        }

        val layoutChangePassword = findViewById<LinearLayout>(R.id.layout_change_password)
        layoutChangePassword.setOnClickListener {
            Toast.makeText(this, "Change password clicked", Toast.LENGTH_SHORT).show()
        }

        val switchFaceId = findViewById<SwitchMaterial>(R.id.switch_face_id)
        switchFaceId.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Face ID is " + (if (isChecked) "on" else "off"), Toast.LENGTH_SHORT).show()
        }

        val switchTouchId = findViewById<SwitchMaterial>(R.id.switch_touch_id)
        switchTouchId.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Touch ID is " + (if (isChecked) "on" else "off"), Toast.LENGTH_SHORT).show()
        }

        val aiSwitch = findViewById<SwitchMaterial>(R.id.ai_switch)
        aiSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "AI access is " + (if (isChecked) "on" else "off"), Toast.LENGTH_SHORT).show()
        }

        val tvSeeMore = findViewById<TextView>(R.id.tv_see_more)
        tvSeeMore.setOnClickListener {
            Toast.makeText(this, "See more clicked", Toast.LENGTH_SHORT).show()
        }

        val itemClearCache = findViewById<LinearLayout>(R.id.item_clear_cache)
        itemClearCache.setOnClickListener {
            Toast.makeText(this, "Clear cache clicked", Toast.LENGTH_SHORT).show()
        }

        val itemDeleteAccount = findViewById<LinearLayout>(R.id.item_delete_account)
        itemDeleteAccount.setOnClickListener {
            Toast.makeText(this, "Delete account clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
