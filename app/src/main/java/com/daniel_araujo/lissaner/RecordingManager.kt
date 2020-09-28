package com.daniel_araujo.lissaner

import com.daniel_araujo.lissaner.rec.*
import java.io.OutputStream

class RecordingManager : AutoCloseable {
    /**
     * The recording manager can be in any of these states.
     */
    private enum class State {
        /**
         * No recording session is active and storage is empty.
         */
        EMPTY,

        /**
         * Recording session is active.
         */
        RECORDING,

        /**
         * No active recording session but there is something in storage.
         */
        PAUSED
    }

    /**
     * Current state.
     */
    private var state: State = State.EMPTY

    /**
     * Callbacks for creating specific objects.
     */
    private var int: RecordingManagerInt

    /**
     * Current recording session. Not null when state is RECORDING.
     */
    private var recordingSession: RecordingSession? = null

    /**
     * Storage. Not null when state is RECORDING or PAUSED.
     */
    private var storage: Storage? = null

    /**
     * Recording session configuration.
     */
    private val config: RecordingSessionConfig

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

    /**
     * Change sample rate.
     */
    var sampleRate: Int
        get() = config.sampleRate
        set(value) {
            if (state != State.EMPTY) {
                throw Exception("Expected to be empty.")
            }

            if (value == config.sampleRate) {
                return
            }

            config.sampleRate = value
        }

    /**
     * Change bits per sample.
     */
    var bitsPerSample: Int
        get() = config.bitsPerSample
        set(value) {
            if (state != State.EMPTY) {
                throw Exception("Expected to be empty.")
            }

            if (value == config.bitsPerSample) {
                return
            }

            config.bitsPerSample = value
        }

    /**
     * Change number of channels.
     */
    var channels: Int
        get() = config.channels
        set(value) {
            if (state != State.EMPTY) {
                throw Exception("Expected to be empty.")
            }

            if (value == config.channels) {
                return
            }

            config.channels = value
        }

    constructor(int: RecordingManagerInt) {
        this.int = int

        config = RecordingSessionConfig()

        config.apply {
            samplesListener = { data ->
                synchronized(storage!!) {
                    storage!!.feed(data)

                    onAccumulateListener?.invoke()
                }
            }

            errorListener = { ex ->
                onRecordError?.invoke(ex)
            }
        }
    }

    fun startRecording() {
        if (state == State.PAUSED || state == State.EMPTY) {
            config.setRecordingBufferSizeInMilliseconds(1000)

            if (state == State.EMPTY) {
                storage = int.createStorage(config)
            }

            recordingSession = int.createSession(config)

            state = State.RECORDING

            onRecordStart?.invoke()
        }
    }

    fun stopRecording() {
        if (state == State.RECORDING) {
            recordingSession!!.close()
            recordingSession = null

            if (storage!!.size() == 0) {
                storage = null
                state = State.EMPTY
            } else {
                state = State.PAUSED
            }

            onRecordStop?.invoke()
        }
    }

    fun isRecording(): Boolean {
        return state == State.RECORDING
    }

    fun saveRecording(stream: OutputStream) {
        if (state == State.RECORDING || state == State.PAUSED) {
            if (storage!!.size() == 0) {
                // Can't do anything.
                return
            }

            val wav = PCM2WAV(
                stream,
                config.channels,
                config.sampleRate,
                config.bitsPerSample)

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
    }

    /**
     * Empties storage.
     */
    fun discardRecording() {
        if (state == State.RECORDING || state == State.PAUSED) {
            if (storage!!.size() > 0) {
                storage!!.clear()

                onAccumulateListener?.invoke()
            }

            if (state == State.PAUSED) {
                state = State.EMPTY
                storage = null
            }
        }
    }

    /**
     * How much time has been accumulated in temporary storage.
     */
    fun accumulated(): Long {
        return when (state) {
            State.RECORDING, State.PAUSED -> PcmUtils.duration(
                storage!!.size(),
                config.sampleRate,
                config.bytesPerSample,
                config.channels
            )

            else -> 0
        }
    }

    override fun close() {
        if (state == State.RECORDING || state == State.PAUSED) {
            recordingSession?.close()
            storage = null
        }

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