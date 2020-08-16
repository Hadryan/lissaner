package com.daniel_araujo.always_recording_microphone.ui

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import android.view.View
import android.widget.Button
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupDev()
    }

    fun onClickRecordingStart(view: View) {
        toggleRecordingService()
    }

    fun onClickDev(view: View) {
        startActivity(Intent(this, DevAudioRecordCombinationsActivity::class.java))
    }

    private fun toggleRecordingService() {
        if (!getOurApplication().isServiceRunning(RecordingService::class.java)) {
            startRecordingService()
        } else {
            stopRecordingService()
        }
    }

    private fun startRecordingService() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.RECORD_AUDIO)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    Intent(this@MainActivity, RecordingService::class.java).also {
                        startService(it)
                    }
                }
            })
            .check();
    }

    private fun stopRecordingService() {
        Intent(this@MainActivity, RecordingService::class.java).also {
            stopService(it)
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