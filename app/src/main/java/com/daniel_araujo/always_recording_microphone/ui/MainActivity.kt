package com.daniel_araujo.always_recording_microphone.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import com.daniel_araujo.always_recording_microphone.AutoServiceBind
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

class MainActivity : Activity() {
    lateinit var recordingService: AutoServiceBind<RecordingService>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordingService = AutoServiceBind(RecordingService::class, this)

        setupDev()
    }

    fun onClickRecordingStart(view: View) {
        toggleRecordingService()
    }

    fun onClickDev(view: View) {
        startActivity(Intent(this, DevAudioRecordCombinationsActivity::class.java))
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
                    }
                }
            })
            .check();
    }

    private fun stopRecordingService() {
        Log.v(javaClass.simpleName, "stopRecordingService")

        recordingService.run {
            it.stopRecording()
        }
    }

    private fun setupDev() {
        val button = findViewById<Button>(R.id.dev)

        if (getOurApplication().showDebugFeatures()) {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.GONE
        }
    }
}