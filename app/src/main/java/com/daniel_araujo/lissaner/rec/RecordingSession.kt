package com.daniel_araujo.lissaner.rec

import com.daniel_araujo.lissaner.PcmUtils
import java.nio.ByteBuffer

/**
 * Base class for recording sessions. A recording sessions begins as soon as the class is created.
 * It stops once the class is disposed (by calling close).
 */
abstract class RecordingSession : AutoCloseable {
    /**
     * Sample rate of this session.
     */
    val sampleRate: Int
        get() = config.sampleRate

    /**
     * Bits per sample.
     */
    val bitsPerSample: Int
        get() = config.bitsPerSample

    /**
     * How many bytes a sample takes.
     */
    val bytesPerSample: Int
        get() = config.bytesPerSample

    /**
     * Number of channels.
     */
    val channels: Int
        get() = config.channels

    /**
     * Total bytes in a second.
     */
    val bytesPerSecond: Int
        get() = config.bytesPerSecond

    /**
     * Byte buffer where you must write samples to.
     */
    protected lateinit var samplesBuffer: ByteBuffer

    /**
     * Local copy of config.
     */
    private var config: RecordingSessionConfig

    /**
     * Creates a recording session. You don't need to override the constructor.
     */
    constructor(config: RecordingSessionConfig) {
        this.config = RecordingSessionConfig(config)

        try {
            // Now we can create our own buffer.
            samplesBuffer = ByteBuffer.wrap(ByteArray(decideBufferSize(this.config)))

            open()
        } catch (e: Exception) {
            emitError(e)
        }
    }

    /**
     * Gets called to open the recording session. You must override this method. Your code
     * must call samplesListener whenever you reach
     */
    protected abstract fun open()

    /**
     * Decides the final size of the buffer.
     */
    open protected fun decideBufferSize(config: RecordingSessionConfig): Int {
        val requestedSize = config.recordingBufferSizeRequest;

        if (requestedSize != null) {
            return requestedSize
        } else {
            return PcmUtils.bufferSize(1000, config.sampleRate, config.bytesPerSample, config.channels)
        }
    }

    /**
     * Notifies samples listener if buffer is filled and clears buffer.
     */
    protected fun flush() {
        if (!samplesBuffer.hasRemaining()) {
            // Only call listener when buffer is completely filled.
            config.samplesListener?.invoke(samplesBuffer)

            // Rewinding buffer.
            samplesBuffer.position(0)
        }
    }

    /**
     * Notifies error listener about an exception.
     */
    protected fun emitError(e: Exception) {
        config.errorListener?.invoke(e)
    }

    /**
     * Clean up code goes here.
     */
    abstract override fun close()
}