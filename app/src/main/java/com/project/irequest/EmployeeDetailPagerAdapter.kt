package com.project.irequest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.irequest.data.models.Employee

class EmployeeDetailPagerAdapter(fa: FragmentActivity, private val employee: Employee) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> EmployeeInfoFragment.newInstance(employee)
            1 -> EmployeeScheduleFragment.newInstance(employee)
            2 -> EmployeeAssetsFragment.newInstance(employee)
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}