package com.daniel_araujo.always_recording_microphone.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.daniel_araujo.always_recording_microphone.AutoServiceBind
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.RecordingService
import com.daniel_araujo.always_recording_microphone.ui.dev.DevAudioRecordCombinationsActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

class MainActivity : Activity() {
    lateinit var recordingService: AutoServiceBind<RecordingService>

    lateinit var buttonRecord: RecButtonView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordingService = AutoServiceBind(RecordingService::class, this)

        buttonRecord = findViewById<RecButtonView>(R.id.button_record).also {
            it.setOnClickListener {
                toggleRecordingService()
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
}