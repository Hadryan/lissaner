package com.daniel_araujo.always_recording_microphone.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.daniel_araujo.always_recording_microphone.AutoServiceBind
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.RecordingService
import com.daniel_araujo.always_recording_microphone.ui.dev.DevAudioRecordCombinationsActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import java.io.File
import java.io.IOException

class MainActivity : Activity() {
    lateinit var recordingService: AutoServiceBind<RecordingService>

    lateinit var buttonRecord: RecButtonView

    lateinit var buttonSave: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordingService = AutoServiceBind(RecordingService::class, this)

        buttonRecord = findViewById<RecButtonView>(R.id.button_record).also {
            it.setOnClickListener {
                toggleRecordingService()
            }
        }

        buttonSave = findViewById<ImageButton>(R.id.button_save).also {
            it.setOnClickListener {
                saveRecordingService()
            }
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

        Dexter.withContext(this)
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
            buttonRecord.isActivated = false;
        }
    }

    private fun saveRecordingService() {
        Log.v(javaClass.simpleName, "saveRecordingService")

        recordingService.run { service ->
            try {
                val file = File(applicationContext.getExternalFilesDir(null), "recording.wav")

                Log.d(javaClass.simpleName, "Saving to ${file.absolutePath}")

                file.outputStream().use { stream ->
                    service.saveRecording(stream)
                }
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: " + e.toString())
            }
        }
    }
}