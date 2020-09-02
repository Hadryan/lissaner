package com.daniel_araujo.always_recording_microphone.android

import android.media.AudioRecord
import com.daniel_araujo.always_recording_microphone.rec.RecordingSession
import com.daniel_araujo.always_recording_microphone.rec.RecordingSessionConfig
import java.nio.ByteBuffer

class AndroidRecordingSession : RecordingSession, AutoCloseable {
    /**
     * Android provides this API to get access to microphone.
     */
    private lateinit var recorder: AudioRecord

    /**
     * Listener for samples.
     */
    private lateinit var samplesListener: (ByteBuffer) -> Unit

    /**
     * Listener for errors.
     */
    private var errorListener: ((Exception) -> Unit)? = null

    /**
     * The size of the samples buffer.
     */
    private var bufferSize: Int = 0

    /**
     * Byte buffer that holds samples for the listener.
     */
    private lateinit var samplesBuffer: ByteBuffer

    constructor(config: RecordingSessionConfig) {
        samplesListener = config.samplesListener!!
        errorListener = config.errorListener

        bufferSize = decideBufferSize(config)

        // Now we can create our own buffer.
        samplesBuffer = ByteBuffer.allocateDirect(bufferSize)

        recorder = AudioRecord(
            config.source,
            config.sampleRate,
            config.channel,
            config.encoding,
            bufferSize
        )

        val frameSize = bufferSize / config.bytesPerSample()

        recorder.setPositionNotificationPeriod(frameSize)

        recorder.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {
                TODO("Not yet implemented")
            }

            override fun onPeriodicNotification(p0: AudioRecord?) {
                val read = recorder.read(samplesBuffer, bufferSize)

                if (read > 0) {
                    samplesBuffer.position(samplesBuffer.position() + read)

                    if (!samplesBuffer.hasRemaining()) {
                        // Only call listener when buffer is completely filled.
                        samplesListener.invoke(samplesBuffer)

                        // Clearing buffer. Listeners must make copies.
                        samplesBuffer.rewind()
                    }
                } else if (read == 0) {
                    // Read nothing apparently.
                } else {
                    // Error.
                    if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                        errorListener?.invoke(Exception(AudioRecord::ERROR_INVALID_OPERATION.name))
                    } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                        errorListener?.invoke(Exception(AudioRecord::ERROR_BAD_VALUE.name))
                    } else if (read == AudioRecord.ERROR_DEAD_OBJECT) {
                        errorListener?.invoke(Exception(AudioRecord::ERROR_DEAD_OBJECT.name))
                    } else if (read == AudioRecord.ERROR) {
                        errorListener?.invoke(Exception(AudioRecord::ERROR.name))
                    } else {
                        errorListener?.invoke(Exception("Unknown error code."))
                    }
                }
            }
        })

        recorder.startRecording()
    }

    /**
     * Decides best buffer.
     */
    private fun decideBufferSize(config: RecordingSessionConfig): Int {
        // We can request the API to tell us the minimum size for its buffer. The buffer cannot be
        // shorter than this.
        val minBufferSize = AudioRecord.getMinBufferSize(config.sampleRate, config.channel, config.encoding)

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
        recorder.release()
    }
}