package com.daniel_araujo.lissaner

import com.daniel_araujo.lissaner.rec.PureMemoryStorage
import com.daniel_araujo.lissaner.rec.RecordingSession
import com.daniel_araujo.lissaner.rec.RecordingSessionConfig
import com.daniel_araujo.lissaner.rec.Storage
import org.junit.Test

import org.junit.Assert.*
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

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
    open class RecordingSessionSilence: RecordingSession {
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
                config.bytesPerSample,
                config.channels)
            val buffer = ByteBuffer.allocate(oneSecond)
            buffer.position(oneSecond)
            config.samplesListener?.invoke(buffer)
        }

        override fun close() {
            closed = true
        }
    }

    /**
     * Generate silence samples in a separate thread.
     */
    class RecordingSessionThreadedSilence: RecordingSessionSilence {
        var running: Boolean = false

        val timer: Timer

        lateinit var task: TimerTask

        constructor(config: RecordingSessionConfig) : super(config) {
            timer = Timer("RecordingSessionThreadedSilence")
            task = object : TimerTask() {
                override fun run() {
                    if (!running) {
                        task.cancel();
                        return;
                    }

                    generate(100)
                }
            }
        }

        fun start() {
            if (running) {
                return;
            }

            running = true

            timer.scheduleAtFixedRate(task, 0, 1)
        }

        fun stop() {
            if (!running) {
                return;
            }

            running = false
        }

        override fun close() {
            super.close()
            stop()
            timer.cancel()
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
    fun onAccumulateListener_callsListenerWhenStorageReceivesSamples() {
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
                    config.bytesPerSample,
                    config.channels)
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
    fun onAccumulateListener_callsListenerOnSave() {
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
                    config.bytesPerSample,
                    config.channels)
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

        recorder.saveRecording(NullOutputStream())

        assertEquals(2, calls.size)
    }

    @Test
    fun onAccumulateListener_callsListenerWhenStorageIsDiscarded() {
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
                    config.bytesPerSample,
                    config.channels)
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

        recorder.discardRecording()

        assertEquals(2, calls.size)
    }

    @Test
    fun onAccumulateListener_isNotCalledIfStorageisEmpty() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        val calls = ArrayList<Long>()

        recorder.onAccumulateListener = {
            calls.add(recorder.accumulated())
        }

        recorder.startRecording()

        assertEquals(0, calls.size)

        recorder.discardRecording()
        recorder.saveRecording(NullOutputStream())

        assertEquals(0, calls.size)
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
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()
        recorder.stopRecording()

        assertTrue(session.closed)
    }

    @Test
    fun saveRecording_whileSessionIsStillActive() {
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
                    config.bytesPerSample,
                    config.channels)
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
    fun saveRecording_doesNotInterruptRecording() {
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
                    config.bytesPerSample,
                    config.channels)
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
    fun saveRecording_doesNotAccumulateSamplesWhileSaving() {
        // This test relies on a race condition.

        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                val session = RecordingSessionThreadedSilence(config)
                session.start()
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    6000000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.use {
            recorder.startRecording()
            Thread.sleep(1)
            recorder.saveRecording(NullOutputStream())
        }
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
                    config.bytesPerSample,
                    config.channels)
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
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.discardRecording()

        assertEquals(0, recorder.accumulated())
    }

    @Test
    fun onRecordStart_isCalledWhenStartRecordingIsCalled() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        var called = 0

        recorder.onRecordStart = {
            called += 1
        }

        assertEquals(0, called)

        recorder.startRecording()

        assertEquals(1, called)
    }

    @Test
    fun onRecordStart_isNotCalledIfStartRecordingIsCalledTheSecondTime() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        var called = 0

        recorder.onRecordStart = {
            called += 1
        }

        assertEquals(0, called)

        recorder.startRecording()
        recorder.startRecording()

        assertEquals(1, called)
    }

    @Test
    fun onRecordStop_isCalledWhenStopRecordingIsCalled() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        var called = 0

        recorder.onRecordStop = {
            called += 1
        }

        recorder.startRecording()

        assertEquals(0, called)

        recorder.stopRecording()

        assertEquals(1, called)
    }

    @Test
    fun onRecordStop_isNotCalledWhenStopRecordingIsCalledTheSecondTime() {
        val recorder = RecordingManager(object: RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels)
                return PureMemoryStorage(oneSecond)
            }
        })

        var called = 0

        recorder.onRecordStop = {
            called += 1
        }

        recorder.startRecording()

        assertEquals(0, called)

        recorder.stopRecording()
        recorder.stopRecording()

        assertEquals(1, called)
    }
}