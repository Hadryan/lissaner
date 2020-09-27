package com.daniel_araujo.lissaner.rec

import android.media.AudioFormat
import org.junit.Test

import org.junit.Assert.*

class RecordingSessionConfigTest {
    @Test
    fun bytesPerSample_8Bits() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 8

        assertEquals(1, config.bytesPerSample);
    }

    @Test
    fun bytesPerSample_16Bits() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 16

        assertEquals(2, config.bytesPerSample);
    }

    @Test
    fun bytesPerSecond_takesBytesPerSampleIntoAccount() {
        val config = RecordingSessionConfig()

        // 1 byte.
        config.bitsPerSample = 8

        assertEquals(44100, config.bytesPerSecond);

        // 2 bytes.
        config.bitsPerSample = 16

        assertEquals(88200, config.bytesPerSecond);
    }

    @Test
    fun bytesPerSecond_takesSampleRateIntoAccount() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 8
        config.sampleRate = 11000

        assertEquals(11000, config.bytesPerSecond);

        config.sampleRate = 44100

        assertEquals(44100, config.bytesPerSecond);
    }

    @Test
    fun bytesPerSecond_numberOfChannelsIntoAccount() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 8
        config.channels = 2
        config.sampleRate = 11000

        assertEquals(22000, config.bytesPerSecond);

        config.channels = 1
        config.sampleRate = 44100

        assertEquals(44100, config.bytesPerSecond);
    }

    @Test
    fun setRecordingBufferSizeInMilliseconds_usesCurrentSampleRateBytesPerSampleAndChannels() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 8
        config.channels = 1
        config.sampleRate = 11000

        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(33000, config.recordingBufferSizeRequest);

        // 2 bytes per sample.
        config.bitsPerSample = 16
        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(66000, config.recordingBufferSizeRequest);

        // 2 channels.
        config.bitsPerSample = 8
        config.channels = 2
        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(66000, config.recordingBufferSizeRequest);
    }

    @Test
    fun setRecordingBufferSizeInMilliseconds_changingSampleRateWillNotAutomaticallyUpdateBufferSize() {
        val config = RecordingSessionConfig()
        config.bitsPerSample = 8
        config.channels = 1
        config.sampleRate = 11000

        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(33000, config.recordingBufferSizeRequest);

        // 2 bytes per sample.
        config.sampleRate = 22000

        assertEquals(33000, config.recordingBufferSizeRequest);
    }
}