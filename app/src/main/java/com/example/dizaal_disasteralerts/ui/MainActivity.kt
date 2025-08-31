package com.example.dizaal_disasteralerts.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.dizaal_disasteralerts.R
import com.example.dizaal_disasteralerts.databinding.ActivityMainBinding
import com.example.dizaal_disasteralerts.ui.emergency.EmergencyFragment
import com.example.dizaal_disasteralerts.ui.home.HomeFragment
import com.example.dizaal_disasteralerts.ui.map.MapFragment
import com.example.dizaal_disasteralerts.ui.profile.ProfileFragment
import com.example.dizaal_disasteralerts.ui.settings.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter


        binding.viewPager.isUserInputEnabled = true


        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> binding.viewPager.currentItem = 0
                R.id.nav_map -> binding.viewPager.currentItem = 1
                R.id.nav_emergency -> binding.viewPager.currentItem = 2
                //R.id.nav_settings -> binding.viewPager.currentItem = 3
                R.id.nav_profile -> binding.viewPager.currentItem = 4
            }
            true
        }


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }
}
