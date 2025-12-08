package com.project.irequest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InfoFragment()
            1 -> PerformanceFragment()
            2 -> ScheduleFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}
