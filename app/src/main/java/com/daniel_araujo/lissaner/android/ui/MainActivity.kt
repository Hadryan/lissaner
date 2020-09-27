package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.daniel_araujo.lissaner.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        if (!ourApplication.splash) {
            val nav = findNavController(R.id.fragment)

            val graph = nav.navInflater.inflate(R.navigation.nav_main)
            graph.startDestination = R.id.splashFragment

            nav.graph = graph
        }
    }
}