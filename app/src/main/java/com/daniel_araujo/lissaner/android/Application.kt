package com.daniel_araujo.lissaner.android

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import com.daniel_araujo.lissaner.files.RecordingFiles

class Application : android.app.Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_FOREGROUND_SERVICE = "channel_0"
    }

    /**
     * Application's RecordingFiles object.
     */
    val recordingFiles: RecordingFiles by lazy {
        AndroidRecordingFiles(applicationContext)
    }

    /**
     * Whether to show features meant for debugging.
     */
    fun showDebugFeatures() : Boolean {
        return Debug.isDebuggerConnected()
    }

    /**
     * Verifies if a service is running.
     */
    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}