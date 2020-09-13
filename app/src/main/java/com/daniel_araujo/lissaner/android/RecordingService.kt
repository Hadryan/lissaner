package com.daniel_araujo.lissaner.android

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.daniel_araujo.lissaner.PcmUtils
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.RecordingManagerInt
import com.daniel_araujo.lissaner.rec.*
import com.daniel_araujo.lissaner.android.ui.MainActivity


class RecordingService : Service() {
    /**
     * Notification id of foreground service. Foreground services are required to launch a
     * notification.
     */
    private val SERVICE_NOTIFICATION_ID = 1

    /**
     * The recording object. This is public on purpose.
     */
    lateinit var recording : RecordingManager

    override fun onCreate() {
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
                            30 * 60 * 1000,
                            config.sampleRate,
                            config.bytesPerSample(),
                            config.channels()
                        )
                    )
                }
            })

        recording.onRecordStart = {
            requestToBeForeground()
        }

        recording.onRecordStop = {
            stopForeground(true)
        }
    }

    override fun onDestroy() {
        recording.close()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return AutoServiceBinder(
            this
        )
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
            .setContentTitle("Lissaner")
            .setContentText("Open the app to stop recording.")
            .setContentIntent(contentIntent)
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val chan = NotificationChannel(Application.NOTIFICATION_CHANNEL_FOREGROUND_SERVICE, getText(
            R.string.app_name
        ), NotificationManager.IMPORTANCE_LOW)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
    }
}