package com.daniel_araujo.always_recording_microphone.android.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.daniel_araujo.always_recording_microphone.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : Activity() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        viewPager = findViewById(R.id.view_pager)

        viewPager.adapter = MainActivityFragmentStateAdapter(this)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getText(R.string.main_activity_tab_info)
                1 -> getText(R.string.main_activity_tab_record)
                2 -> getText(R.string.main_activity_tab_files)
                else -> "Tab"
            }
        }.attach()
    }
}

class MainActivityFragmentStateAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InfoFragment()
            1 -> RecordFragment()
            2 -> Fragment()
            else -> Fragment()
        }
    }
}
