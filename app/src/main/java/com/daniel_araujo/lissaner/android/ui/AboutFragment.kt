package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.BuildConfig
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.android.ExternalIntentUtils

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TopView>(R.id.top).setLeftButton(R.drawable.ic_arrow_left, View.OnClickListener {
            findNavController().popBackStack()
        })

        view.findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME

        view.findViewById<Button>(R.id.button_store).setOnClickListener {
            ExternalIntentUtils.goToAppPlayStore(ourActivity, BuildConfig.APPLICATION_ID)
        }

        view.findViewById<Button>(R.id.button_source).setOnClickListener {
            ExternalIntentUtils.openWebPage(ourActivity, "https://github.com/daniel-araujo/lissaner")
        }

        view.findViewById<Button>(R.id.button_licenses).setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_licensesFragment)
        }
    }
}