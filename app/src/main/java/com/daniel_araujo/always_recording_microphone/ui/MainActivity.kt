package com.daniel_araujo.always_recording_microphone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.daniel_araujo.always_recording_microphone.R
import com.daniel_araujo.always_recording_microphone.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.recording_start_button)

        button.setOnClickListener {
            startRecordingService();
        }
    }

    override fun onStart() {
        super.onStart()
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
}