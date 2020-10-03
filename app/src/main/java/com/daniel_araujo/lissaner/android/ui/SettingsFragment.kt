package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.ByteFormatUtils
import com.daniel_araujo.lissaner.PcmUtils
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.TimestampUtils
import com.daniel_araujo.lissaner.android.*
import com.shawnlin.numberpicker.NumberPicker

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

        view.findViewById<TopView>(R.id.top).setLeftButton(R.drawable.ic_arrow_left, View.OnClickListener {
            findNavController().popBackStack()
        })

        view.findViewById<Button>(R.id.about_button).setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_aboutFragment)
        }

        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

        // Memory.
        run {
            val memory = view.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.memory)

            val options = arrayListOf<Long>(
                // Index 0 is ignored.
                0,

                TimestampUtils.minutesToMilli(1),
                TimestampUtils.minutesToMilli(2),
                TimestampUtils.minutesToMilli(5)
            )
            // Add multiples of 10.
            for (i in 1..15) {
                options.add(TimestampUtils.minutesToMilli(i * 10L).toLong())
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

                updateEstimatedMemoryUsage()
            }
        }

        updateEstimatedMemoryUsage()

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

                    updateEstimatedMemoryUsage()
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

                    updateEstimatedMemoryUsage()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val recordingService = AutoServiceBind(
            RecordingService::class,
            requireContext()
        )

        recordingService.run { enableRecordingOptions(!it.recording.isRecording()) }
    }

    private fun enableRecordingOptions(enable: Boolean) {
        val memory = requireView().findViewById<View>(R.id.memory)
        val sps = requireView().findViewById<View>(R.id.samples_per_second)
        val bps = requireView().findViewById<View>(R.id.bits_per_sample)
        val warning = requireView().findViewById<View>(R.id.recording_warning)

        memory.isEnabled = enable
        sps.isEnabled = enable
        bps.isEnabled = enable
        warning.visibility = if (enable) View.GONE else View.VISIBLE
    }

    private fun updateEstimatedMemoryUsage() {
        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

        val estimated = requireView().findViewById<TextView>(R.id.estimated)

        val size = PcmUtils.bufferSize(
            PreferenceUtils.getLongOrFail(preferences, Application.PREFERENCE_KEEP),
            PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_SAMPLES_PER_SECOND),
            Math.ceil(PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_BITS_PER_SAMPLE).toDouble() / 8).toInt(),
            1)

        estimated.text = ByteFormatUtils.shortSize(requireContext(), size)
    }
}