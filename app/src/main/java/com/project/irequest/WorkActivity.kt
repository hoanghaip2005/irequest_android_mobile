package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.tabs.TabLayout
import com.irequest.utils.RoleName
import com.irequest.utils.SessionManager
import com.project.irequest.databinding.ActivityWorkBinding

/**
 * WorkActivity - Quản lý yêu cầu theo vai trò
 * - User/Requester: Xem "Yêu cầu của tôi"
 * - Agent: Xem "Công việc của tôi" + "Chưa gán"
 * - Approver: Xem "Chờ phê duyệt"
 * - Admin: Xem tất cả
 */
class WorkActivity : BaseActivity() {

    private lateinit var binding: ActivityWorkBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Setup navigation từ BaseActivity
        setupBottomNavigation()
        setSupportActionBar(binding.toolbar)
        setActiveTab(1)

        // Setup NavHostFragment
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_work) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup role-based TabLayout
        setupRoleTabLayout(navController)
    }

    private fun setupRoleTabLayout(navController: androidx.navigation.NavController) {
        val tabLayout = binding.roleTabLayout
        val userRole = sessionManager.getUserRole()

        // Setup tab click listener
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> navController.navigate(R.id.nav_my_requests_work)
                    1 -> navController.navigate(R.id.nav_assigned_requests)
                    2 -> navController.navigate(R.id.nav_unassigned_requests)
                    3 -> navController.navigate(R.id.nav_approval_queue)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // TODO: Uncomment role-based tab visibility when ready
        // For now, show all tabs for testing purposes
        /*
        // Ẩn tabs không cần thiết dựa vào role
        when (userRole) {
            RoleName.USER -> {
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
            }

            RoleName.AGENT -> {
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
            }

            RoleName.APPROVER -> {
                tabLayout.getTabAt(0)?.view?.visibility = View.GONE
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.view?.visibility = View.GONE
            }

            RoleName.ADMIN -> {
                // ADMIN thấy tất cả 4 tabs
            }

            else -> {
                tabLayout.getTabAt(1)?.view?.visibility = View.GONE
                tabLayout.getTabAt(2)?.view?.visibility = View.GONE
                tabLayout.getTabAt(3)?.view?.visibility = View.GONE
            }
        }
        */

        // Select tab mặc định
        val defaultTab = when (userRole) {
            RoleName.USER -> 0
            RoleName.AGENT -> 1
            RoleName.APPROVER -> 3
            else -> 0
        }
        tabLayout.selectTab(tabLayout.getTabAt(defaultTab))
    }

    // Override navigation methods từ BaseActivity
    override fun onNavigationHomeClicked() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onNavigationWorkClicked() {
        // Đã ở trang công việc rồi
        Toast.makeText(this, "Bạn đang ở trang Quản lý yêu cầu", Toast.LENGTH_SHORT).show()
        setActiveTab(1)
    }

    override fun onNavigationChatClicked() {
        // Chuyển đến ChatActivity
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }

    override fun onNavigationAccountClicked() {
        // TODO: Chuyển đến AccountActivity
        Toast.makeText(this, "Chuyển đến trang Tài khoản", Toast.LENGTH_SHORT).show()
        setActiveTab(3)
    }
}