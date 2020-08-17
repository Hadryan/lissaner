package com.daniel_araujo.always_recording_microphone.rec

import android.media.AudioRecord
import java.nio.ByteBuffer

class AndroidRecordingSession : AutoCloseable {
    /**
     * Android provides this API to get access to microphone.
     */
    private lateinit var recorder: AudioRecord

    /**
     * Listener for samples.
     */
    private lateinit var samplesListener: (ByteBuffer) -> Unit

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

        //AudioRecord.getMinBufferSize(config.sampleRate, config.channel, config.encoding)

        // We can request the API to tell us the minimum size for its buffer.
        bufferSize = config.bytesPerSecond() * 10;

        // Now we can create our own buffer.
        samplesBuffer = ByteBuffer.allocateDirect(bufferSize)

        recorder = AudioRecord(
            config.source,
            config.sampleRate,
            config.channel,
            config.encoding,
            bufferSize
        )

        recorder.setPositionNotificationPeriod(100)

        recorder.setRecordPositionUpdateListener(object : AudioRecord.OnRecordPositionUpdateListener {
            override fun onMarkerReached(p0: AudioRecord?) {
                TODO("Not yet implemented")
            }

            override fun onPeriodicNotification(p0: AudioRecord?) {
                val read = recorder.read(samplesBuffer, bufferSize)

                if (read > 0) {
                    samplesBuffer.position(read)

                    samplesListener.invoke(samplesBuffer)

                    samplesBuffer.rewind()
                } else {
                    // Some sort of error.
                    assert(read == 0)
                }
            }
        })

        recorder.startRecording()
    }

    override fun close() {
        recorder.release()
    }
}