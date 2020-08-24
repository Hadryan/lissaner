package com.daniel_araujo.always_recording_microphone

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.daniel_araujo.always_recording_microphone.rec.*
import com.daniel_araujo.always_recording_microphone.ui.MainActivity
import java.io.OutputStream


class RecordingService : Service() {
    /**
     * Notification id of foreground service. Foreground services are required to launch a
     * notification.
     */
    private val SERVICE_NOTIFICATION_ID = 1

    private lateinit var recording : RecordingManager

    var onAccumulateListener: (() -> Unit)?
        get() = recording.onAccumulateListener
        set(listener) { recording.onAccumulateListener = listener }

    override fun onCreate() {
        recording = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return AndroidRecordingSession(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                return PureMemoryStorage(
                    PcmUtils.bufferSize(
                        10 * 60 * 1000,
                        config.sampleRate,
                        config.bytesPerSample(),
                        config.channels()))
            }
        })
    }

    override fun onDestroy() {
        recording.close()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return AutoServiceBinder(this)
    }

    fun startRecording() {
        requestToBeForeground()

        recording.startRecording()
    }

    fun stopRecording() {
        stopForeground(true)

        recording.stopRecording()
    }

    fun isRecording(): Boolean {
        return recording.isRecording()
    }

    fun saveRecording(stream: OutputStream) {
        recording.saveRecording(stream)
    }

    fun discardRecording() {
        recording.discardRecording()
    }

    fun accumulated(): Long {
        return recording.accumulated()
    }

    /**
     * Requests Android to turn this service into a foreground service.
     */
    private fun requestToBeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Without a custom channel, this version of android fails. Previous versions fall back
            // to the default channel.
            createNotificationChannel()
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, Application.NOTIFICATION_CHANNEL_FOREGROUND_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Always Recording Microphone")
            .setContentText("Open the app to stop recording.")
            .setContentIntent(contentIntent)
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val chan = NotificationChannel(Application.NOTIFICATION_CHANNEL_FOREGROUND_SERVICE, getText(R.string.app_name), NotificationManager.IMPORTANCE_LOW)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }
}