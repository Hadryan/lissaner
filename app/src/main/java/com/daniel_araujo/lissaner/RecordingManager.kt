package com.daniel_araujo.lissaner

import com.daniel_araujo.lissaner.rec.*
import java.io.OutputStream

class RecordingManager : AutoCloseable {
    private lateinit var int: RecordingManagerInt

    private var recordingSession: RecordingSession? = null

    private var storage: Storage? = null

    private var config: RecordingSessionConfig? = null

    /**
     * Called whenever an error occurs during recording.
     */
    var onRecordError: ((Exception) -> Unit)? = null

    /**
     * Called each time more audio is accumulated in storage.
     */
    var onAccumulateListener: (() -> Unit)? = null

    /**
     * Called when recording starts.
     */
    var onRecordStart: (() -> Unit)? = null

    /**
     * Called when recording stops.
     */
    var onRecordStop: (() -> Unit)? = null

    constructor(int: RecordingManagerInt) {
        this.int = int
    }

    fun startRecording() {
        if (recordingSession != null) {
            // Already recording.
            return
        }

        config = RecordingSessionConfig().apply {
            samplesListener = { data ->
                synchronized(storage!!) {
                    storage!!.feed(data)

                    onAccumulateListener?.invoke()
                }
            }

            errorListener = { ex ->
                onRecordError?.invoke(ex)
            }

            setRecordingBufferSizeInMilliseconds(1000)
        }

        if (storage == null) {
            storage = int.createStorage(config!!)
        }

        recordingSession = int.createSession(config!!)

        onRecordStart?.invoke()
    }

    fun stopRecording() {
        if (recordingSession == null) {
            // Already stopped.
            return
        }

        recordingSession?.close()
        recordingSession = null
        onRecordStop?.invoke()
    }

    fun isRecording(): Boolean {
        return recordingSession != null
    }

    fun saveRecording(stream: OutputStream) {
        if (storage!!.size() == 0) {
            return;
        }

        val wav = PCM2WAV(
            stream,
            config!!.channels(),
            config!!.sampleRate,
            config!!.bytesPerSample() * 8)

        wav.use {
            synchronized(storage!!) {
                wav.expectSize(storage!!.size())
                storage!!.move {
                    wav.feed(it)
                }
            }
        }

        onAccumulateListener?.invoke()
    }

    /**
     * Empties storage.
     */
    fun discardRecording() {
        if (storage != null) {
            if (storage!!.size() > 0) {
                storage!!.clear()

                onAccumulateListener?.invoke()
            }
        }
    }

    /**
     * How much time has been accumulated in temporary storage.
     */
    fun accumulated(): Long {
        if (storage != null) {
            return PcmUtils.duration(
                storage!!.size(),
                config!!.sampleRate,
                config!!.bytesPerSample(),
                config!!.channels())
        } else {
            return 0
        }
    }

    override fun close() {
        recordingSession?.close()

        storage = null

        onRecordError = null
        onRecordStart = null
        onRecordStop = null
        onAccumulateListener = null
    }
}

interface RecordingManagerInt {
    fun createSession(config: RecordingSessionConfig): RecordingSession

    fun createStorage(config: RecordingSessionConfig): Storage
}