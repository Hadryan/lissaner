package com.daniel_araujo.always_recording_microphone.rec

import android.media.AudioFormat
import android.media.MediaRecorder
import com.daniel_araujo.always_recording_microphone.BuildConfig
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
     * Returns number of bytes per second.
     */
    fun bytesPerSecond(): Int {
        val bytesPerSample = when (encoding) {
            AudioFormat.ENCODING_PCM_FLOAT,
            AudioFormat.ENCODING_PCM_16BIT -> 2

            AudioFormat.ENCODING_PCM_8BIT -> 1

            else -> error("Should never reach this branch.")
        }

        val channels = when (channel) {
            AudioFormat.CHANNEL_IN_MONO -> 1
            AudioFormat.CHANNEL_IN_STEREO -> 2

            else -> error("Should never reach this branch.")
        }

        return bytesPerSample * channels * sampleRate
    }
}
