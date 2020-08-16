package com.daniel_araujo.always_recording_microphone

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.nio.ByteBuffer

class RecordingSession : AutoCloseable {
    /**
     * Audio sample rate. That's how many samples you get per second.
     */
    var sampleRate: Int = 44100
        get() = field
        set(value) {
            if (!isRunning()) {
                return;
            }

            field = value
        }

    /**
     * Which channels or how many channels.
     */
    var channel: Int = AudioFormat.CHANNEL_IN_MONO
        get() = field
        set(value) {
            if (!isRunning()) {
                return;
            }

            field = value
        }

    /**
     * How a sample is encoded.
     */
    var encoding: Int = AudioFormat.ENCODING_PCM_16BIT
        get() = field
        set(value) {
            if (!isRunning()) {
                return;
            }

            field = value
        }

    /**
     * Android provides this API to get access to microphone.
     */
    private var recorder: AudioRecord? = null

    /**
     * Listener for samples.
     */
    private var samplesListener: ((ByteBuffer) -> Unit)? = null

    /**
     * The size of the samples buffer.
     */
    private var bufferSize: Int = 0

    /**
     * Byte buffer that holds samples for the listener.
     */
    private var samplesBuffer: ByteBuffer ?= null

    fun start() {
        if (isRunning()) {
            // Nothing to do.
            return;
        }

        // We can request the API to tell us the minimum size for its buffer.
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, encoding) * 10;

        // Now we can create our own buffer.
        samplesBuffer = ByteBuffer.allocateDirect(bufferSize)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channel,
            encoding,
            bufferSize
        )

        recorder?.setPositionNotificationPeriod(10)

        recorder?.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {
                TODO("Not yet implemented")
            }

            override fun onPeriodicNotification(p0: AudioRecord?) {
                val read = recorder!!.read(samplesBuffer!!, bufferSize)

                if (read > 0) {
                    samplesBuffer!!.position(read)

                    samplesListener!!.invoke(samplesBuffer!!)

                    samplesBuffer!!.rewind()
                } else {
                    // Some sort of error.
                    assert(read == 0)
                }
            }
        })

        recorder?.startRecording()
    }

    fun stop() {
        if (!isRunning()) {
            // Nothing to do.
            return;
        }

        recorder?.release();
    }

    fun isRunning(): Boolean {
        return recorder != null
    }

    fun setSamplesListener(listener: (ByteBuffer) -> Unit) {
        samplesListener = listener
    }

    override fun close() {
        stop()
    }
}