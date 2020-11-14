package com.daniel_araujo.lissaner.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeneralReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("BootCompletedReceiver", "Received action: " + intent.action)

        val application = context.applicationContext as Application

        application.initialize()

        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            val preferences = application.getDefaultSharedPreferences()

            val autoStart = PreferenceUtils.getBooleanOrFail(
                preferences,
                Application.PREFERENCE_AUTO_START
            )

            if (autoStart) {
                application.recording.startRecording()
            }
        }
    }
}