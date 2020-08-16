package com.daniel_araujo.always_recording_microphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {
    /**
     * Permission request code for all permissions.
     */
    val ASK_RECORD_AUDIO_ID = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.recording_start_button)

        button.setOnClickListener {
            askPermission()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ASK_RECORD_AUDIO_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startRecordingService()
                } else {
                    Toast.makeText(applicationContext, "Failed to get permission to record.", Toast.LENGTH_SHORT);
                }
            }

            else -> {
                // Ignore.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Requests all permissions for the app if necessary.
     */
    private fun askPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), ASK_RECORD_AUDIO_ID);
        } else {
            startRecordingService()
        }
    }

    private fun startRecordingService() {
        Intent(this, RecordingService::class.java).also {
            startService(it)
        }
    }
}