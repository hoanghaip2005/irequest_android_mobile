package com.project.irequest

import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupBottomNavigation() {
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navWork = findViewById<LinearLayout>(R.id.navWork)
        val navChat = findViewById<LinearLayout>(R.id.navChat)
        val navAccount = findViewById<LinearLayout>(R.id.navAccount)

        navHome?.setOnClickListener { onNavigationHomeClicked() }
        navWork?.setOnClickListener { onNavigationWorkClicked() }
        navChat?.setOnClickListener { onNavigationChatClicked() }
        navAccount?.setOnClickListener { onNavigationAccountClicked() }
    }

    protected fun setActiveTab(tabIndex: Int) {
        // Reset tất cả các tab về trạng thái không active
        resetAllTabs()

        // Set tab được chọn là active
        when (tabIndex) {
            0 -> setTabActive(R.id.navHome, R.id.iconHome, R.id.textHome)
            1 -> setTabActive(R.id.navWork, R.id.iconWork, R.id.textWork)
            2 -> setTabActive(R.id.navChat, R.id.iconChat, R.id.textChat)
            3 -> setTabActive(R.id.navAccount, R.id.iconAccount, R.id.textAccount)
        }
    }

    private fun resetAllTabs() {
        resetTab(R.id.iconHome, R.id.textHome)
        resetTab(R.id.iconWork, R.id.textWork)
        resetTab(R.id.iconChat, R.id.textChat)
        resetTab(R.id.iconAccount, R.id.textAccount)
    }

    private fun resetTab(iconId: Int, textId: Int) {
        findViewById<ImageView>(iconId)?.alpha = 0.5f
        findViewById<TextView>(textId)?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
    }

    private fun setTabActive(navId: Int, iconId: Int, textId: Int) {
        findViewById<ImageView>(iconId)?.alpha = 1.0f
        findViewById<TextView>(textId)?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
    }

    // Abstract methods để các Activity con override
    protected open fun onNavigationHomeClicked() {}
    protected open fun onNavigationWorkClicked() {}
    protected open fun onNavigationChatClicked() {}
    protected open fun onNavigationAccountClicked() {}
}
