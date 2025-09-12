package com.example.dizaal_disasteralerts.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dizaal_disasteralerts.ui.emergency.EmergencyFragment
import com.example.dizaal_disasteralerts.ui.home.HomeFragment
import com.example.dizaal_disasteralerts.ui.map.MapFragment
import com.example.dizaal_disasteralerts.ui.profile.ProfileFragment
import com.example.dizaal_disasteralerts.ui.settings.SettingsFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> HomeFragment()
        1 -> MapFragment()
        2 -> EmergencyFragment()
        3 -> SettingsFragment()
        4 -> ProfileFragment()
        else -> HomeFragment()
    }
}
