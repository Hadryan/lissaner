package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.os.StatFs
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.ByteFormatUtils
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.android.AutoServiceBind
import com.daniel_araujo.lissaner.android.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import java.io.IOException
import java.util.*

class RecordingFragment : Fragment() {
    /**
     * We only have a service so that we can record with the app closed.
     */
    var recordingService: AutoServiceBind<RecordingService>? = null

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

        recordingService?.run { service ->
            try {
                val name = System.currentTimeMillis().toString() + ".wav"
                val stream = ourActivity.ourApplication.recordingFiles.create(name);

                Log.d(javaClass.simpleName, "Creating ${name}")

                stream.use {
                    service.recording.saveRecording(stream)
                }
            } catch (e: IOException) {
                Log.e(javaClass.simpleName, "File write failed.", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        recordingService = AutoServiceBind(
            RecordingService::class,
            requireActivity().application
        )

        recordingService?.run {
            it.recording.onAccumulateListener = {
                accumulatedTime.time = it.recording.accumulated()
            }

            // Synchronize accumulated time.
            accumulatedTime.time = it.recording.accumulated()
        }
    }

    override fun onPause() {
        super.onPause()

        recordingService?.run {
            it.recording.onAccumulateListener = null
        }
    }
}