package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import androidx.navigation.findNavController
import com.daniel_araujo.lissaner.R

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!ourApplication.initialized) {
            val nav = findNavController(R.id.fragment)

            val graph = nav.navInflater.inflate(R.navigation.nav_main)
            graph.startDestination = R.id.splashFragment

            nav.graph = graph
        }
    }
}