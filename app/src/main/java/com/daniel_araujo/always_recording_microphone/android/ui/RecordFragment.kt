package com.daniel_araujo.always_recording_microphone.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.daniel_araujo.always_recording_microphone.android.AutoServiceBind
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.android.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import java.io.File
import java.io.IOException

class RecordFragment : Fragment() {
    lateinit var recordingService: AutoServiceBind<RecordingService>

    lateinit var buttonRecord: RecButtonView

    lateinit var buttonSave: ImageButton

    lateinit var accumulatedTime: TextCounter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordingService =
            AutoServiceBind(
                RecordingService::class,
                activity!!
            )

        recordingService.onConnectListener = { service ->
            service.onAccumulateListener = {
                accumulatedTime.time = service.accumulated()
            }
        }

        accumulatedTime = view.findViewById<TextCounter>(R.id.accumulated_time)

        buttonRecord = view.findViewById<RecButtonView>(R.id.button_record).also {
            it.setOnClickListener {
                toggleRecordingService()
            }
        }

        buttonSave = view.findViewById<ImageButton>(R.id.button_save).also {
            it.setOnClickListener {
                saveRecordingService()
            }
        }

        view.findViewById<ImageButton>(R.id.button_delete).also {
            it.setOnClickListener {
                discardRecording()
            }
        }

        recordingService.run {
            // Synchronize recording button state.
            buttonRecord.isActivated = it.isRecording();

            // Synchronize accumulated time.
            accumulatedTime.time = it.accumulated()
        }
    }

    private fun toggleRecordingService() {
        recordingService.run {
            if (!it.isRecording()) {
                startRecordingService()
            } else {
                stopRecordingService()
            }
        }
    }

    private fun startRecordingService() {
        Log.v(javaClass.simpleName, "startRecordingService")

        Dexter.withContext(activity)
            .withPermission(android.Manifest.permission.RECORD_AUDIO)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    recordingService.run {
                        it.startRecording()
                        buttonRecord.isActivated = true;
                    }
                }
            })
            .check();
    }

    private fun stopRecordingService() {
        Log.v(javaClass.simpleName, "stopRecordingService")

        recordingService.run {
            it.stopRecording()
            accumulatedTime.time = it.accumulated()
            buttonRecord.isActivated = false;
        }
    }

    private fun saveRecordingService() {
        Log.v(javaClass.simpleName, "saveRecordingService")

        recordingService.run { service ->
            try {
                val file = File(context!!.getExternalFilesDir(null), System.currentTimeMillis().toString() + ".wav")

                Log.d(javaClass.simpleName, "Saving to ${file.absolutePath}")

                file.outputStream().use { stream ->
                    service.saveRecording(stream)
                }
            } catch (e: IOException) {
                Log.e("Exception", "File write failed.", e)
            }
        }
    }

    private fun discardRecording() {
        Log.v(javaClass.simpleName, "saveRecordingService")

        recordingService.run {
            it.discardRecording()
        }
    }
}