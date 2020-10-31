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

        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

        // Memory.
        run {
            val memory = view.findViewById<SettingsOptionSelectNumberView>(R.id.memory)

            val options = arrayListOf<Int>(
                TimestampUtils.minutesToMilli(1).toInt(),
                TimestampUtils.minutesToMilli(2).toInt(),
                TimestampUtils.minutesToMilli(5).toInt()
            )
            // Add multiples of 10.
            for (i in 1..15) {
                options.add(TimestampUtils.minutesToMilli(i * 10L).toInt())
            }

            memory.available = options.toList()

            memory.formatter = {
                TimestampUtils.milliToMinutes(it.toLong()).toString()
            }

            memory.value = PreferenceUtils.getLongOrFail(preferences, Application.PREFERENCE_KEEP).toInt()

            memory.onValueChangedListener = {
                with(preferences.edit()) {
                    putLong(Application.PREFERENCE_KEEP, memory.value.toLong())
                    commit()
                }

                updateEstimatedMemoryUsage()
            }
        }

        updateEstimatedMemoryUsage()

        // Samples per second
        run {
            val sps = view.findViewById<SettingsOptionSelectValueView>(R.id.samples_per_second)

            sps.available = AudioRecordUtils.supportedSampleRates()

            sps.value = PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_SAMPLES_PER_SECOND)

            sps.onValueChangedListener = {
                with(preferences.edit()) {
                    putInt(Application.PREFERENCE_SAMPLES_PER_SECOND, it as Int)
                    commit()
                }

                updateEstimatedMemoryUsage()
            }
        }

        // Bits per sample
        run {
            val bps = view.findViewById<SettingsOptionSelectValueView>(R.id.bits_per_sample)

            bps.available = AudioRecordUtils.supportedPcmBits()

            bps.value = PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_BITS_PER_SAMPLE)

            bps.onValueChangedListener = {
                with(preferences.edit()) {
                    putInt(Application.PREFERENCE_BITS_PER_SAMPLE, it as Int)
                    commit()
                }

                updateEstimatedMemoryUsage()
            }
        }

        // Auto-start
        run {
            val bps = view.findViewById<SettingsOptionSwitchView>(R.id.auto_start)

            bps.value = PreferenceUtils.getBooleanOrFail(preferences, Application.PREFERENCE_AUTO_START)

            bps.onValueChangedListener = {
                with(preferences.edit()) {
                    putBoolean(Application.PREFERENCE_AUTO_START, it)
                    commit()
                }

                updateEstimatedMemoryUsage()
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

        val size = PcmUtils.bufferSize(
            PreferenceUtils.getLongOrFail(preferences, Application.PREFERENCE_KEEP),
            PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_SAMPLES_PER_SECOND),
            Math.ceil(PreferenceUtils.getIntOrFail(preferences, Application.PREFERENCE_BITS_PER_SAMPLE).toDouble() / 8).toInt(),
            1)

        view?.findViewById<SettingsOptionSelectNumberView>(R.id.memory)?.also {
            it.description =
                "Estimated size: " + ByteFormatUtils.shortSize(requireContext(), size)
        }
    }
}