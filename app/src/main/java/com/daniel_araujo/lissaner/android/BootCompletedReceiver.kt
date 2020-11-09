package com.daniel_araujo.lissaner.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("BootCompletedReceiver", "Received action: " + intent.action)

        val application = context.applicationContext as Application

        application.initialize()

        val preferences = application.getDefaultSharedPreferences()

        val autoStart = PreferenceUtils.getBooleanOrFail(
            preferences,
            Application.PREFERENCE_AUTO_START
        )

        if (autoStart) {
            // Will most likely work in the context of the service.
            application.recording.startRecording()
        }
    }
}