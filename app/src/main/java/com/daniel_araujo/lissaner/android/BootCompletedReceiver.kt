package com.daniel_araujo.lissaner.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.v("BootCompletedReceiver", "Starting up application from boot.")

            val application = context.applicationContext as Application

            application.initialize()

            val preferences = application.getDefaultSharedPreferences()

            val autoStart = PreferenceUtils.getBooleanOrFail(
                preferences,
                Application.PREFERENCE_AUTO_START
            )

            if (autoStart) {
                // Will most like work in the context of the service.
                application.recording.startRecording()
            }
        }
    }
}