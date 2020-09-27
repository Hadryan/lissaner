package com.daniel_araujo.lissaner.rec

import android.media.AudioFormat
import android.media.MediaRecorder
import java.nio.ByteBuffer

class RecordingSessionConfig {
    /**
     * Audio sample rate. That's how many samples you get per second.
     */
    var sampleRate: Int = 44100

    /**
     * How many channels to record from source.
     */
    var channels: Int = 1

    /**
     * Sets number of bits per sample.
     */
    var bitsPerSample: Int = 16

    /**
     * Listener for samples.
     */
    var samplesListener: ((ByteBuffer) -> Unit)? = null

    /**
     * Listener for errors.
     */
    var errorListener: ((Exception) -> Unit)? = null

    /**
     * How many samples in bytes to send to the listener. If null then the recording session decides
     * the size.
     */
    var recordingBufferSizeRequest: Int? = null

    /**
     * Returns number of bytes per second.
     */
    val bytesPerSecond: Int
        get() {
            return sampleRate * bytesPerSample * channels
        }

    /**
     * Returns how many bytes a sample occupies.
     */
    val bytesPerSample: Int
        get() {
            return Math.ceil(bitsPerSample.toDouble() / 8).toInt()
        }

    /**
     * Sets recording buffer size in milliseconds. Uses existing config values to calculate size.
     */
    fun setRecordingBufferSizeInMilliseconds(milli: Long) {
        recordingBufferSizeRequest = (milli * sampleRate * bytesPerSample * channels / 1000).toInt()
    }
}
