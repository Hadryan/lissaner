package com.daniel_araujo.lissaner.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Debug
import com.daniel_araujo.lissaner.files.RecordingFiles

class Application : android.app.Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_FOREGROUND_SERVICE = "channel_0"

        const val PREFERENCE_KEEP = "keep"
        const val PREFERENCE_SAMPLES_PER_SECOND = "samples_per_second"
        const val PREFERENCE_BITS_PER_SAMPLE = "bits_per_sample"
        const val PREFERENCE_AUTO_START = "auto_start"
    }

    /**
     * Whether our initialization code has run.
     */
    var initialized: Boolean = false

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
     * Returns the SharedPreferences instance that Android Jetpack's preference framework uses.
     */
    fun getDefaultSharedPreferences(): SharedPreferences {
        return applicationContext.getSharedPreferences(
            applicationContext.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
    }

    /**
     * Application initialization must go here. It will be called in appropriate places.
     */
    fun initialize() {
        if (initialized) {
            // Nothing to do.
            return;
        }

        val preferences = getDefaultSharedPreferences()

        with(preferences.edit()) {
            if (!com.daniel_araujo.lissaner.android.PreferenceUtils.hasLong(preferences, PREFERENCE_KEEP)) {
                putLong(PREFERENCE_KEEP, 30 * 60 * 1000)
            }

            if (!com.daniel_araujo.lissaner.android.PreferenceUtils.hasInt(preferences, PREFERENCE_SAMPLES_PER_SECOND)) {
                putInt(PREFERENCE_SAMPLES_PER_SECOND, 44100)
            }

            if (!com.daniel_araujo.lissaner.android.PreferenceUtils.hasInt(preferences, PREFERENCE_BITS_PER_SAMPLE)) {
                putInt(PREFERENCE_BITS_PER_SAMPLE, 16)
            }

            commit()
        }

        initialized = true
    }
}