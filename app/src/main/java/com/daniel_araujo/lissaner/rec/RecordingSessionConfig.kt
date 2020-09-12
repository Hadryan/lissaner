package com.daniel_araujo.lissaner.rec

import android.media.AudioFormat
import android.media.MediaRecorder
import java.nio.ByteBuffer

class RecordingSessionConfig {
    /**
     * Audio source.
     */
    var source: Int = MediaRecorder.AudioSource.MIC

    /**
     * Audio sample rate. That's how many samples you get per second.
     */
    var sampleRate: Int = 44100

    /**
     * Which channels or how many channels.
     */
    var channel: Int = AudioFormat.CHANNEL_IN_MONO

    /**
     * How a sample is encoded.
     */
    var encoding: Int = AudioFormat.ENCODING_PCM_16BIT

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
    fun bytesPerSecond(): Int {
        return sampleRate * bytesPerSample() * channels()
    }

    /**
     * Returns how many bytes a sample occupies.
     */
    fun bytesPerSample(): Int {
        return when (encoding) {
            AudioFormat.ENCODING_PCM_FLOAT,
            AudioFormat.ENCODING_PCM_16BIT -> 2

            AudioFormat.ENCODING_PCM_8BIT -> 1

            else -> error("Should never reach this branch.")
        }
    }

    /**
     * Returns number of channels.
     */
    fun channels(): Int {
        return when (channel) {
            AudioFormat.CHANNEL_IN_MONO -> 1
            AudioFormat.CHANNEL_IN_STEREO -> 2

            else -> error("Should never reach this branch.")
        }
    }

    /**
     * Sets recording buffer size in milliseconds.
     */
    fun setRecordingBufferSizeInMilliseconds(milli: Long) {
        recordingBufferSizeRequest = (milli * sampleRate * bytesPerSample() * channels() / 1000).toInt()
    }
}
