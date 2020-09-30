package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.IOUtils
import com.daniel_araujo.lissaner.R

class LicensesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_licenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TopView>(R.id.top).setLeftButton(R.drawable.ic_arrow_left, View.OnClickListener {
            findNavController().popBackStack()
        })

        val layout = view.findViewById<LinearLayout>(R.id.layout)

        val licenses = requireActivity().application.assets.list("licenses")

        licenses?.forEach { filename ->
            requireActivity().application.assets.open("licenses/$filename").use {
                val entryView = LicenseEntryView(requireContext())
                entryView.name = filename
                entryView.text = IOUtils.readAll(it)

                layout.addView(entryView)
            }
        }
    }
}