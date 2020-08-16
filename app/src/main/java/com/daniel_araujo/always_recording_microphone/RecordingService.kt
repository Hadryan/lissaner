package com.daniel_araujo.always_recording_microphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class RecordingService : Service() {
    /**
     * Notification id of foreground service. Foreground services are required to launch a
     * notification.
     */
    private val SERVICE_NOTIFICATION_ID = 1

    /**
     * Android provides this API to get access to microphone.
     */
    lateinit var recorder: AudioRecord

    /**
     * The size of the samples buffer.
     */
    var bufferSize: Int = 0

    override fun onCreate() {
        // We can request the API to tell us the minimum size for its buffer.
        bufferSize = AudioRecord.getMinBufferSize(AudioFormat.SAMPLE_RATE_UNSPECIFIED, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT);

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioFormat.SAMPLE_RATE_UNSPECIFIED,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        recorder.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {
                TODO("Not yet implemented")
            }

            override fun onPeriodicNotification(p0: AudioRecord?) {
                val bufferedAudio = ByteArray(bufferSize)
                val read: Int = recorder.read(bufferedAudio, 0, bufferSize)

                Log.d(javaClass.simpleName, "Read $read bytes worth of samples.");
            }
        })

        recorder.startRecording()

        requestToBeForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        //recorder.stop()
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
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

        val notification: Notification = NotificationCompat.Builder(this, Application.NOTIFICATION_CHANNEL_FOREGROUND_SERVICE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("TITLE")
            .setContentText("TEXT")
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