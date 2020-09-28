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
    class RecordingSessionFail(config: RecordingSessionConfig) : RecordingSession(config) {
        override fun open() {
            emitError(Exception("Mock"))
        }

        override fun close() {}
    }

    /**
     * This implementation always sends silence samples.
     */
    open class RecordingSessionSilence(config: RecordingSessionConfig) : RecordingSession(config) {
        /**
         * Whether the session has been closed.
         */
        var closed: Boolean = false

        /**
         * Generates silence samples.
         */
        fun generate(duration: Long) {
            if (closed) {
                return
            }

            var size = PcmUtils.bufferSize(
                duration,
                sampleRate,
                bytesPerSample,
                channels
            )

            while (size > 0) {
                val toSend = Math.min(size, samplesBuffer.remaining())

                // Don't have to write to the buffer. It's filled with 0s by default.
                samplesBuffer.position(samplesBuffer.position() + toSend)

                flush()

                size -= toSend
            }
        }

        override fun open() {}

        override fun close() {
            closed = true
        }
    }

    /**
     * Generate silence samples in a separate thread.
     */
    class RecordingSessionThreadedSilence : RecordingSessionSilence {
        var running: Boolean = false

        val timer: Timer

        lateinit var task: TimerTask

        constructor(config: RecordingSessionConfig) : super(config) {
            timer = Timer("RecordingSessionThreadedSilence")
            task = object : TimerTask() {
                override fun run() {
                    synchronized(running) {
                        if (!running) {
                            task.cancel();
                            return;
                        }

                        generate(100)
                    }
                }
            }
        }

        fun start() {
            synchronized(running) {
                if (running) {
                    return;
                }

                running = true

                timer.scheduleAtFixedRate(task, 0, 1)
            }
        }

        fun stop() {
            synchronized(running) {
                if (!running) {
                    return;
                }

                running = false
            }
        }

        override fun close() {
            synchronized(running) {
                super.close()
                stop()
                timer.cancel()
            }
        }
    }

    @Test
    fun onRecordError_isCalledWhenRecordingFails() {
        val recorder = RecordingManager(object : RecordingManagerInt {
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
    fun onAccumulateListener_callsListenerEverySecond() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    2000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        val calls = ArrayList<Long>()

        recorder.onAccumulateListener = {
            calls.add(recorder.accumulated())
        }

        recorder.startRecording()

        session.generate(2000)

        assertEquals(2, calls.size)
        assertEquals(1000, calls[0])
        assertEquals(2000, calls[1])
    }

    @Test
    fun onAccumulateListener_shouldTakeNewSampleRateIntoAccount() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        val calls = ArrayList<Long>()

        recorder.onAccumulateListener = {
            calls.add(recorder.accumulated())
        }

        recorder.bitsPerSample = 8000

        recorder.startRecording()

        session.generate(1000)

        assertEquals(1, calls.size)
        assertEquals(1000, calls[0])
    }

    @Test
    fun onAccumulateListener_callsListenerOnSave() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    5000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
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
                    config.channels
                )
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
    fun saveRecording_afterStoppingWithSomeSamples() {
        // This test relies on a race condition.
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.use {
            recorder.startRecording()
            session.generate(1000)
            recorder.stopRecording()
            recorder.saveRecording(NullOutputStream())
        }
    }

    @Test
    fun startRecording_createsStorageIfEmptied() {
        val calls = ArrayList<Long>()

        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                calls.add(System.currentTimeMillis())
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.stopRecording()

        recorder.discardRecording()

        recorder.startRecording()

        assertEquals(2, calls.size)
    }

    @Test
    fun startRecording_doesNotRecreateStorageIfRecordingIsJustPaused() {
        val calls = ArrayList<Long>()

        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                calls.add(System.currentTimeMillis())
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.stopRecording()

        recorder.startRecording()

        assertEquals(1, calls.size)
    }

    @Test
    fun stopRecording_doesNotDiscardWhatIsInStorage() {
        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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
    fun startRecording_doesNotRecreateStorageIfStillRecording() {
        val calls = ArrayList<Long>()

        lateinit var session: RecordingSessionSilence

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                session = RecordingSessionSilence(config)
                return session
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                calls.add(System.currentTimeMillis())
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        session.generate(1000)

        assertEquals("Must have something in storage.", 1000, recorder.accumulated())

        recorder.discardRecording()

        assertEquals("Must have nothing in storage.", 0, recorder.accumulated())

        session.generate(1000)

        assertEquals("Must have something in storage again.", 1000, recorder.accumulated())

        assertEquals(1, calls.size)
    }

    @Test
    fun onRecordStart_isCalledWhenStartRecordingIsCalled() {
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
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

    @Test
    fun sampleRate_createsSessionWithNewSampleRate() {
        var configSampleRate: Int? = null

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                configSampleRate = config.sampleRate
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        assertEquals(44100, recorder.sampleRate)

        recorder.sampleRate = 8000

        recorder.startRecording()

        assertEquals(8000, configSampleRate)
    }

    @Test(expected = Exception::class)
    fun sampleRate_throwsExceptionIfAlreadyRecording() {
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        recorder.sampleRate = 8000
    }

    @Test
    fun bitsPerSample_createsSessionWithNewSampleRate() {
        var configBitsPerSample: Int? = null

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                configBitsPerSample = config.bitsPerSample
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        assertEquals(16, recorder.bitsPerSample)

        recorder.bitsPerSample = 8

        recorder.startRecording()

        assertEquals(8, configBitsPerSample)
    }

    @Test(expected = Exception::class)
    fun bitsPerSample_throwsExceptionIfAlreadyRecording() {
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        recorder.bitsPerSample = 8000
    }

    @Test
    fun channels_createsSessionWithNewSampleRate() {
        var configChannels: Int? = null

        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                configChannels = config.channels
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        assertEquals(1, recorder.channels)

        recorder.channels = 2

        recorder.startRecording()

        assertEquals(2, configChannels)
    }

    @Test(expected = Exception::class)
    fun channels_throwsExceptionIfAlreadyRecording() {
        val recorder = RecordingManager(object : RecordingManagerInt {
            override fun createSession(config: RecordingSessionConfig): RecordingSession {
                return RecordingSessionSilence(config)
            }

            override fun createStorage(config: RecordingSessionConfig): Storage {
                val oneSecond = PcmUtils.bufferSize(
                    1000,
                    config.sampleRate,
                    config.bytesPerSample,
                    config.channels
                )
                return PureMemoryStorage(oneSecond)
            }
        })

        recorder.startRecording()

        recorder.channels = 2
    }
}