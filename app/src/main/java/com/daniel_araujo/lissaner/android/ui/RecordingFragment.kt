package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.daniel_araujo.lissaner.R
import java.io.IOException

class RecordingFragment : Fragment() {
    /**
     * A view that shows how many seconds have been recorded.
     */
    lateinit var accumulatedTime: TextCounter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accumulatedTime = view.findViewById<TextCounter>(R.id.accumulated_time)

        view.findViewById<Button>(R.id.button_save).also {
            it.setOnClickListener {
                saveRecording()
            }
        }
    }

    private fun saveRecording() {
        Log.v(javaClass.simpleName, "saveRecording")

        try {
            val name = System.currentTimeMillis().toString() + ".wav"
            val stream = ourActivity.ourApplication.recordingFiles.create(name)

            Log.d(javaClass.simpleName, "Creating ${name}")

            stream.use {
                ourActivity.ourApplication.recording.saveRecording(stream)
            }
        } catch (e: IOException) {
            Log.e(javaClass.simpleName, "File write failed.", e)
        }
    }

    override fun onResume() {
        super.onResume()

        ourActivity.ourApplication.recording.onAccumulateListener = {
            updateRecordingTime()
        }

        updateRecordingTime()
    }

    override fun onPause() {
        super.onPause()

        ourActivity.ourApplication.recording.onAccumulateListener = null
    }

    fun updateRecordingTime() {
        accumulatedTime.time = ourActivity.ourApplication.recording.accumulated()
    }
}