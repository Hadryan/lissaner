package com.daniel_araujo.always_recording_microphone

import com.daniel_araujo.always_recording_microphone.rec.PureMemoryStorage
import com.daniel_araujo.always_recording_microphone.rec.RecordingSession
import com.daniel_araujo.always_recording_microphone.rec.RecordingSessionConfig
import com.daniel_araujo.always_recording_microphone.rec.Storage
import org.junit.Test

import org.junit.Assert.*
import java.nio.ByteBuffer

class RecordManagerTest {
    /**
     * This implementation always invokes the error listener.
     */
    class RecordingSessionFail: RecordingSession {
        constructor(config: RecordingSessionConfig) {
            config.errorListener?.invoke(Exception("Mock"))
        }

        override fun close() {}
    }

    /**
     * This implementation always sends silence samples.
     */
    class RecordingSessionSilence: RecordingSession {
        /**
         * Whether the session has been closed.
         */
        var closed: Boolean = false

        var config: RecordingSessionConfig

        constructor(config: RecordingSessionConfig) {
            this.config = config
        }

        /**
         * Generates silence samples.
         */
        fun generate(duration: Long) {
            val oneSecond = PcmUtils.bufferSize(
                duration,
                config.sampleRate,
                config.bytesPerSample(),
                config.channels())
            val buffer = ByteBuffer.allocate(oneSecond)
            buffer.position(oneSecond)
            config.samplesListener?.invoke(buffer)
        }

        override fun close() {
            closed = true
        }
    }

    @Test
    fun callsErrorListenerWhenRecordingFails() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionFail(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                return PureMemoryStorage(1000)
            }
        })

        val calls = ArrayList<Exception>()

        recorder.onRecordError = {
            calls.add(it)
        }

        recorder.startRecording()

        assertEquals(1, calls.size)
        assertEquals("Mock", calls[0].message)
    }

    @Test
    fun callsAccumulateListenerWhenStorageReceivesSamples() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        val calls = ArrayList<Long>()

        recorder.onAccumulateListener = {
            calls.add(recorder.accumulated())
        }

        recorder.startRecording()

        session.generate(1000)

        assertEquals(1, calls.size)
        assertEquals(1000, calls[0])
    }

    @Test
    fun closesSessionWhenRecordingIsStopped() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()
        recorder.stopRecording()

        assertTrue(session.closed)
    }

    @Test
    fun savesRecordingWhileSessionIsStillActive() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.saveRecording(NullOutputStream())

        assertFalse("Session must not be closed.", session.closed)
        assertEquals("Storage must be empty now.", 0, recorder.accumulated())
    }

    @Test
    fun savingDoesNotInterruptRecording() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.saveRecording(NullOutputStream())

        assertFalse("Session must not be closed.", session.closed)

        session.generate(1000)

        assertEquals("Must continue to store new samples in storage.", 1000, recorder.accumulated())
    }

    @Test
    fun doesNotDiscardWhatIsInStorageWhenRecordingIsStopped() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.stopRecording()

        assertEquals("Storage must be intact.", 1000, recorder.accumulated())
    }

    @Test
    fun discardRecording_emptiesStorage() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample(),
                    config.channels())
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.discardRecording()

        assertEquals(0, recorder.accumulated())
    }
}