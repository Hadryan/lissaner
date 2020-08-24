package com.daniel_araujo.always_recording_microphone

import android.util.Log
import com.daniel_araujo.always_recording_microphone.rec.*
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

    constructor(int: RecordingManagerInt) {
        this.int = int
    }

    fun startRecording() {
        config = RecordingSessionConfig().apply {
            samplesListener = { data ->
                storage!!.feed(data)

                onAccumulateListener?.invoke()
            }

            errorListener = { ex ->
                onRecordError?.invoke(ex)
            }

            setRecordingBufferSizeInMilliseconds(1000)
        }

        storage = int.createStorage(config!!)

        recordingSession = int.createSession(config!!)
    }

    fun stopRecording() {
        recordingSession?.close()
        recordingSession = null
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
            wav.feed(storage!!.move())
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
    }
}

interface RecordingManagerInt {
    fun createSession(config: RecordingSessionConfig): RecordingSession

    fun createStorage(config: RecordingSessionConfig): Storage
}