package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.daniel_araujo.lissaner.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProductivityFragment : Fragment() {
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_productivity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = view.findViewById(R.id.view_pager)

        viewPager.adapter = MainActivityFragmentStateAdapter(this)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getText(R.string.main_activity_tab_record)
                1 -> getText(R.string.main_activity_tab_files)
                else -> "Tab"
            }
        }.attach()

        super.onViewCreated(view, savedInstanceState)
    }
}

class MainActivityFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): com.daniel_araujo.lissaner.android.ui.Fragment {
        return when (position) {
            0 -> RecordFragment()
            1 -> FilesFragment()
            else -> com.daniel_araujo.lissaner.android.ui.Fragment()
        }
    }
}
