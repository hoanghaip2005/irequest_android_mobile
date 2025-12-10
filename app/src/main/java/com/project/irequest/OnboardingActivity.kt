package com.project.irequest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.irequest.utils.SessionManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var btnSkip: TextView
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var sessionManager: SessionManager
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        
        // Kiểm tra nếu đã xem onboarding rồi thì skip
        if (sessionManager.isOnboardingCompleted()) {
            navigateToLogin()
            return
        }
        
        setContentView(R.layout.activity_onboarding)
        
        initViews()
        setupViewPager()
        setupClickListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        indicatorLayout = findViewById(R.id.indicatorLayout)
    }

    private fun setupViewPager() {
        val onboardingItems = listOf(
            OnboardingItem(
                imageRes = R.drawable.onboarding1,
                title = "Quản lý yêu cầu dễ dàng",
                description = "Tạo, theo dõi và quản lý các yêu cầu công việc một cách nhanh chóng và hiệu quả"
            ),
            OnboardingItem(
                imageRes = R.drawable.onboarding2,
                title = "Phê duyệt thông minh",
                description = "Quy trình phê duyệt tự động với nhiều cấp độ, giúp đẩy nhanh quá trình xử lý"
            ),
            OnboardingItem(
                imageRes = R.drawable.onboarding3,
                title = "Báo cáo & Phân tích",
                description = "Theo dõi tiến độ, hiệu suất làm việc và thống kê chi tiết theo thời gian thực"
            ),
            OnboardingItem(
                imageRes = R.drawable.onboarding4,
                title = "Cộng tác hiệu quả",
                description = "Chat, bình luận và chia sẻ tài liệu ngay trong từng yêu cầu để làm việc nhóm dễ dàng hơn"
            )
        )
        
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = onboardingAdapter
        
        // Setup indicators
        setupIndicators(onboardingItems.size)
        setCurrentIndicator(0)
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                updateButtonText(position, onboardingItems.size)
            }
        })
    }

    private fun setupIndicators(count: Int) {
        indicatorLayout.removeAllViews()
        val indicators = arrayOfNulls<View>(count)
        
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.indicator_size),
            resources.getDimensionPixelSize(R.dimen.indicator_size)
        )
        layoutParams.setMargins(8, 0, 8, 0)
        
        for (i in indicators.indices) {
            indicators[i] = View(this)
            indicators[i]?.setBackgroundResource(R.drawable.indicator_inactive)
            indicators[i]?.layoutParams = layoutParams
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorLayout.childCount
        for (i in 0 until childCount) {
            val indicator = indicatorLayout.getChildAt(i)
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_active)
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun updateButtonText(position: Int, totalItems: Int) {
        if (position == totalItems - 1) {
            btnNext.text = "Bắt đầu"
            btnSkip.visibility = View.GONE
        } else {
            btnNext.text = "Tiếp tục"
            btnSkip.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingAdapter.itemCount) {
                viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        
        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // Không lưu trạng thái để luôn hiển thị onboarding mỗi lần mở app
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

data class OnboardingItem(
    val imageRes: Int,
    val title: String,
    val description: String
)
