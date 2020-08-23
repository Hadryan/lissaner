package com.daniel_araujo.always_recording_microphone

import android.util.Log
import com.daniel_araujo.always_recording_microphone.rec.*
import java.io.OutputStream

class RecordingManager : AutoCloseable {
    private lateinit var int: RecordingManagerInt

    private var recordingSession: RecordingSession? = null

    private var storage: Storage? = null

    private var config: RecordingSessionConfig? = null

    constructor(int: RecordingManagerInt) {
        this.int = int
    }

    fun startRecording() {
        config = RecordingSessionConfig().apply {
            samplesListener = { data ->
                Log.d(javaClass.simpleName, "Read ${data.position()} bytes worth of samples.");
                storage!!.feed(data)
            }

            errorListener = { ex ->
                Log.d(javaClass.simpleName, "errorListener", ex);
            }

            setRecordingBufferSizeInMilliseconds(5000)
        }

        storage = PureMemoryStorage(
            PcmUtils.bufferSize(
                10000,
                config!!.sampleRate,
                config!!.bytesPerSample(),
                config!!.channels()))

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

    override fun close() {
        recordingSession?.close()
    }
}

interface RecordingManagerInt {
    fun createSession(config: RecordingSessionConfig): RecordingSession
}