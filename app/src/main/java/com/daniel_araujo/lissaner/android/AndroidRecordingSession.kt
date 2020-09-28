package com.daniel_araujo.lissaner.android

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.daniel_araujo.lissaner.rec.RecordingSession
import com.daniel_araujo.lissaner.rec.RecordingSessionConfig
import java.nio.ByteBuffer

class AndroidRecordingSession(config: RecordingSessionConfig) : RecordingSession(config) {
    /**
     * Android provides this API to get access to microphone.
     */
    private lateinit var recorder: AudioRecord

    override fun open() {
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channel(channels),
            encoding(bitsPerSample),
            samplesBuffer.capacity()
        )

        val frameSize = samplesBuffer.capacity() / bytesPerSample

        recorder.setPositionNotificationPeriod(frameSize)

        recorder.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {
                TODO("Not yet implemented")
            }

            override fun onPeriodicNotification(p0: AudioRecord?) {
                try {
                    // If we passed ByteBuffer directly to read we'd get:
                    // Buffer direct access is not supported, can't record
                    val dst = samplesBuffer.array()
                    val read = recorder.read(
                        dst,
                        samplesBuffer.arrayOffset() + samplesBuffer.position(),
                        samplesBuffer.remaining())

                    if (read > 0) {
                        samplesBuffer.position(samplesBuffer.position() + read)

                        flush()
                    } else if (read == 0) {
                        // Read nothing apparently.
                    } else {
                        // Error.
                        if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                            throw Exception(AudioRecord::ERROR_INVALID_OPERATION.name)
                        } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                            throw Exception(AudioRecord::ERROR_BAD_VALUE.name)
                        } else if (read == AudioRecord.ERROR_DEAD_OBJECT) {
                            throw Exception(AudioRecord::ERROR_DEAD_OBJECT.name)
                        } else if (read == AudioRecord.ERROR) {
                            throw Exception(AudioRecord::ERROR.name)
                        } else {
                            throw Exception("Unknown error code.")
                        }
                    }
                } catch (e: Exception) {
                    emitError(e)
                }
            }
        })

        recorder.startRecording()
    }

    override fun decideBufferSize(config: RecordingSessionConfig): Int {
        // We can request the API to tell us the minimum size for its buffer. The buffer cannot be
        // shorter than this.
        val minBufferSize = AudioRecord.getMinBufferSize(config.sampleRate, channel(config.channels), encoding(config.bitsPerSample))

        // This is the size that the config wants us to use.
        val requestedSize = config.recordingBufferSizeRequest;

        if (requestedSize != null) {
            // If the requested size is smaller than the minimum size, we pick the minimum size.
            return Math.max(minBufferSize, requestedSize);
        } else {
            // The config wants us to make a decision.
            return minBufferSize;
        }
    }

    override fun close() {
        recorder.stop()
        recorder.release()
    }

    /**
     * Returns AudioRecord encoding value.
     */
    fun encoding(bitsPerSample: Int): Int {
        return when (bitsPerSample) {
            8 -> AudioFormat.ENCODING_PCM_8BIT

            16 -> AudioFormat.ENCODING_PCM_16BIT

            else -> error("Should never reach this branch.")
        }
    }

    /**
     * Returns AudioRecord channel value.
     */
    fun channel(channels: Int): Int {
        return when (channels) {
            1 -> AudioFormat.CHANNEL_IN_MONO

            2 -> AudioFormat.CHANNEL_IN_STEREO

            else -> error("Should never reach this branch.")
        }
    }
}