package com.daniel_araujo.lissaner.rec

import android.media.AudioFormat
import org.junit.Test

import org.junit.Assert.*

class RecordingSessionConfigTest {
    @Test
    fun bytesPerSample_ENCODING_PCM_16BIT() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_16BIT

        assertEquals(2, config.bytesPerSample());
    }

    @Test
    fun bytesPerSample_ENCODING_PCM_FLOAT() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_FLOAT

        assertEquals(2, config.bytesPerSample());
    }

    @Test
    fun bytesPerSample_ENCODING_PCM_8BIT() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_8BIT

        assertEquals(1, config.bytesPerSample());
    }

    @Test
    fun bytesPerSecond_takesBytesPerSampleIntoAccount() {
        val config = RecordingSessionConfig()

        // 1 byte.
        config.encoding = AudioFormat.ENCODING_PCM_8BIT

        assertEquals(44100, config.bytesPerSecond());

        // 2 bytes.
        config.encoding = AudioFormat.ENCODING_PCM_16BIT

        assertEquals(88200, config.bytesPerSecond());
    }

    @Test
    fun bytesPerSecond_takesSampleRateIntoAccount() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_8BIT
        config.sampleRate = 11000

        assertEquals(11000, config.bytesPerSecond());

        config.sampleRate = 44100

        assertEquals(44100, config.bytesPerSecond());
    }

    @Test
    fun bytesPerSecond_numberOfChannelsIntoAccount() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_8BIT
        config.channel = AudioFormat.CHANNEL_IN_STEREO
        config.sampleRate = 11000

        assertEquals(22000, config.bytesPerSecond());

        config.channel = AudioFormat.CHANNEL_IN_MONO
        config.sampleRate = 44100

        assertEquals(44100, config.bytesPerSecond());
    }

    @Test
    fun channels_CHANNEL_IN_MONO() {
        val config = RecordingSessionConfig()
        config.channel = AudioFormat.CHANNEL_IN_MONO

        assertEquals(1, config.channels());
    }

    @Test
    fun channels_CHANNEL_IN_STEREO() {
        val config = RecordingSessionConfig()
        config.channel = AudioFormat.CHANNEL_IN_STEREO

        assertEquals(2, config.channels());
    }

    @Test
    fun setRecordingBufferSizeInMilliseconds_usesCurrentSampleRateBytesPerSampleAndChannels() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_8BIT
        config.channel = AudioFormat.CHANNEL_IN_MONO
        config.sampleRate = 11000

        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(33000, config.recordingBufferSizeRequest);

        // 2 bytes per sample.
        config.encoding = AudioFormat.ENCODING_PCM_16BIT
        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(66000, config.recordingBufferSizeRequest);

        // 2 channels.
        config.channel = AudioFormat.CHANNEL_IN_STEREO
        config.encoding = AudioFormat.ENCODING_PCM_8BIT
        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(66000, config.recordingBufferSizeRequest);
    }

    @Test
    fun setRecordingBufferSizeInMilliseconds_changingSampleRateWillNotAutomaticallyUpdateBufferSize() {
        val config = RecordingSessionConfig()
        config.encoding = AudioFormat.ENCODING_PCM_8BIT
        config.channel = AudioFormat.CHANNEL_IN_MONO
        config.sampleRate = 11000

        config.setRecordingBufferSizeInMilliseconds(3000)

        assertEquals(33000, config.recordingBufferSizeRequest);

        // 2 bytes per sample.
        config.sampleRate = 22000

        assertEquals(33000, config.recordingBufferSizeRequest);
    }
}