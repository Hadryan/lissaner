package com.daniel_araujo.lissaner.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Debug
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.daniel_araujo.lissaner.PcmUtils
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.RecordingManagerInt
import com.daniel_araujo.lissaner.files.RecordingFiles
import com.daniel_araujo.lissaner.rec.PureMemoryStorage
import com.daniel_araujo.lissaner.rec.RecordingSession
import com.daniel_araujo.lissaner.rec.RecordingSessionConfig
import com.daniel_araujo.lissaner.rec.Storage

class Application : android.app.Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_FOREGROUND_SERVICE = "channel_0"

        const val PREFERENCE_ACTIVATED = "activated"
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
     * We only have a service so that we can record with the app closed.
     */
    lateinit var recordingService: AutoServiceBind<RecordingService>

    /**
     * The recording object.
     */
    lateinit var recording: RecordingManager

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
            Log.d("Initialize", "Another initialization attempt. Ignoring")
            // Nothing to do.
            return;
        }

        Log.d("Initialize", "Begin")

        val preferences = getDefaultSharedPreferences()

        with(preferences.edit()) {
            if (!PreferenceUtils.hasBoolean(preferences, PREFERENCE_ACTIVATED)) {
                putBoolean(PREFERENCE_ACTIVATED, false)
            }

            if (!PreferenceUtils.hasLong(preferences, PREFERENCE_KEEP)) {
                putLong(PREFERENCE_KEEP, 30 * 60 * 1000)
            }

            if (!PreferenceUtils.hasInt(preferences, PREFERENCE_SAMPLES_PER_SECOND)) {
                putInt(PREFERENCE_SAMPLES_PER_SECOND, 44100)
            }

            if (!PreferenceUtils.hasInt(preferences, PREFERENCE_BITS_PER_SAMPLE)) {
                putInt(PREFERENCE_BITS_PER_SAMPLE, 16)
            }

            if (!PreferenceUtils.hasBoolean(preferences, PREFERENCE_AUTO_START)) {
                putBoolean(PREFERENCE_AUTO_START, false)
            }

            commit()
        }

        recordingService = AutoServiceBind(
            RecordingService::class,
            this
        )

        recording =
            RecordingManager(object : RecordingManagerInt {
                override fun createSession(config: RecordingSessionConfig): RecordingSession {
                    return AndroidRecordingSession(
                        config
                    )
                }

                override fun createStorage(config: RecordingSessionConfig): Storage {
                    return PureMemoryStorage(
                        PcmUtils.bufferSize(
                            PreferenceUtils.getLongOrFail(
                                preferences,
                                PREFERENCE_KEEP
                            ),
                            config.sampleRate,
                            config.bytesPerSample,
                            config.channels
                        )
                    )
                }
            })

        recording.onBeforeRecordStart = {
            if (recording.accumulated() == 0L) {
                recording.sampleRate = PreferenceUtils.getIntOrFail(
                    preferences,
                    PREFERENCE_SAMPLES_PER_SECOND
                )
                recording.bitsPerSample = PreferenceUtils.getIntOrFail(
                    preferences,
                    PREFERENCE_BITS_PER_SAMPLE
                )
            }
        }

        recording.onRecordStart = {
            recordingService.run {
                it.requestToBeForeground()
            }

            with(preferences.edit()) {
                putBoolean(PREFERENCE_ACTIVATED, true)

                commit()
            }
        }

        recording.onRecordStop = {
            with(preferences.edit()) {
                putBoolean(PREFERENCE_ACTIVATED, false)

                commit()
            }

            recordingService.run {
                it.stopForeground(true)
            }
        }

        val activated = PreferenceUtils.getBooleanOrFail(
            preferences,
            PREFERENCE_ACTIVATED
        )

        if (activated) {
            recording.startRecording()
        }

        initialized = true

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                Log.d("ProcessLifecycleOwner", "onStart")

                val autoStart = PreferenceUtils.getBooleanOrFail(
                    preferences,
                    PREFERENCE_AUTO_START
                )

                if (autoStart) {
                    recording.startRecording()
                }
            }
        })

        Log.d("Initialize", "End")
    }
}