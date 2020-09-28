package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.TimestampUtils
import com.daniel_araujo.lissaner.android.Application
import com.daniel_araujo.lissaner.android.AudioRecordUtils
import com.daniel_araujo.lissaner.android.PreferenceUtils
import com.daniel_araujo.lissaner.android.SpinnerUtils
import com.shawnlin.numberpicker.NumberPicker
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

        // Memory.
        run {
            val memory = view.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.memory)

            val options = arrayListOf<Long>(
                // Index 0 is ignored.
                0,

                1 * 60 * 1000,
                2 * 60 * 1000,
                5 * 60 * 1000
            )
            // Add multiples of 10.
            for (i in 1..15) {
                options.add((i * 10 * 60 * 1000).toLong())
            }

            memory.maxValue = 15

            memory.formatter = NumberPicker.Formatter {
                TimestampUtils.milliToMinutes(options[it]).toString()
            }

            memory.value = options.indexOf(PreferenceUtils.getLongOrFail(preferences, Application.PREFERENCE_KEEP))

            memory.setOnValueChangedListener { picker, oldVal, newVal ->
                with(preferences.edit()) {
                    putLong(Application.PREFERENCE_KEEP, options[newVal])
                    commit()
                }
            }
        }

        // Samples per second
        run {
            val sps = view.findViewById<Spinner>(R.id.samples_per_second)

            val values = AudioRecordUtils.supportedSampleRates()

            val adapter = ArrayAdapter<Int>(requireContext(), android.R.layout.simple_spinner_item, values)

            sps.adapter = adapter

            SpinnerUtils.selectItemByValue(sps, PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_SAMPLES_PER_SECOND))

            sps.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing.
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    with(preferences.edit()) {
                        putInt(Application.PREFERENCE_SAMPLES_PER_SECOND, adapter.getItem(position)!!)
                        commit()
                    }
                }
            }
        }

        // Bits per sample
        run {
            val bps = view.findViewById<Spinner>(R.id.bits_per_sample)

            val values = AudioRecordUtils.supportedPcmBits()

            val adapter = ArrayAdapter<Int>(requireContext(), android.R.layout.simple_spinner_item, values)

            bps.adapter = adapter

            SpinnerUtils.selectItemByValue(bps, PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_BITS_PER_SAMPLE))

            bps.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing.
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    with(preferences.edit()) {
                        putInt(Application.PREFERENCE_BITS_PER_SAMPLE, adapter.getItem(position)!!)
                        commit()
                    }
                }
            }
        }
    }
}