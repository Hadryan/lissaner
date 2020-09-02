package com.daniel_araujo.always_recording_microphone;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

public class PCM2WAVTest {
    /**
     * Tests with certain combinations.
     */
    @RunWith(Parameterized.class)
    public static class PCM2WAVSamplesTest {
        @Parameterized.Parameters(name = "{index}: channels={0}, sampleRate={1}, bitsPerSample={2}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { 1, 44100, 16 }, { 2, 44100, 16 },

                    { 1, 44100, 8 }, { 2, 44100, 8 },

                    { 1, 8000, 8 }, { 1, 16000, 8 },

                    // Bits per sample not a multiple of 8.
                    { 1, 16000, 4 }, { 1, 16000, 7 },
                    { 1, 16000, 2 }, { 2, 16000, 4 },
            });
        }

        private int channels;
        private int sampleRate;
        private int bitsPerSample;

        public PCM2WAVSamplesTest(int channels, int sampleRate, int bitsPerSample) {
            this.channels = channels;
            this.sampleRate = sampleRate;
            this.bitsPerSample = bitsPerSample;
        }

        @Test
        public void correctFmt() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.feed(generateSilence(1000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, 0x80000000);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
        }

        @Test
        public void expectedDataSize() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, 0x80000000);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);

            assertEquals(0x64617461, dataInput.readInt());
            assertEquals(0x80000000, Integer.reverseBytes(dataInput.readInt()));
        }

        @Test
        public void expectedAmountOfData() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, 0x80000000);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);

            int expectedSize = PcmUtils.INSTANCE.bufferSize(5000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

            assertEquals(0x64617461, dataInput.readInt());
            dataInput.readInt();
            assertEquals(expectedSize, input.available());
        }
    }

    private static void assertRiffHeader(DataInputStream dataInput, int expectedSize) throws IOException {
        // ChunkID
        assertEquals(0x52494646, dataInput.readInt());

        // ChunkSize
        assertEquals(expectedSize, Integer.reverseBytes(dataInput.readInt()));

        // Format
        assertEquals(0x57415645, dataInput.readInt());
    }

    private static void assertFmtChunk(DataInputStream dataInput, int expectedChannels, int expectedSampleRate, int expectedBitsPerSample) throws IOException {
        int expectedBytesPerSample = calcBytesPerSample(expectedBitsPerSample);

        // Subchunk1ID
        assertEquals(0x666d7420, dataInput.readInt());

        // Subchunk1Size
        assertEquals(16, Integer.reverseBytes(dataInput.readInt()));

        // AudioFormat
        assertEquals(1, Short.reverseBytes(dataInput.readShort()));

        // NumChannels
        assertEquals(expectedChannels, Short.reverseBytes(dataInput.readShort()));

        // SampleRate
        assertEquals(expectedSampleRate, Integer.reverseBytes(dataInput.readInt()));

        // ByteRate
        assertEquals(expectedSampleRate * expectedChannels * expectedBytesPerSample, Integer.reverseBytes(dataInput.readInt()));

        // BlockAlign
        assertEquals(expectedChannels * expectedBytesPerSample, Short.reverseBytes(dataInput.readShort()));

        // BitsPerSample
        assertEquals(expectedBitsPerSample, Short.reverseBytes(dataInput.readShort()));
    }

    private static int calcBytesPerSample(int bitsPerSample) {
        return (int) Math.ceil((double) bitsPerSample / 8);
    }

    private static byte[] generateSilence(long duration, int channels, int sampleRate, int bitsPerSample) {
        byte[] buffer = new byte[(int) (duration * channels * sampleRate * calcBytesPerSample(bitsPerSample) / 1000)];

        return buffer;
    }

    @Test
    public void closeWithoutWritingData() throws IOException {
        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream);

        try (PCM2WAV p2w = new PCM2WAV(stream, 1, 44100, 16)) {
        }

        assertEquals(36, input.available());
    }

    @Test
    public void singleDataChunk() throws IOException {
        // I used to create multiple data chunks, each one having 5 seconds of data. Turns out
        // you're only meant to create a single data chunk. Unless I use Wave list Chunk,
        // but I won't.

        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream, 2000000);
        DataInputStream dataInput = new DataInputStream(input);

        final int sampleRate = 8000;
        final int bitsPerSample = 8;
        final int channels = 1;

        try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
            p2w.feed(generateSilence(10000, channels, sampleRate, bitsPerSample));
        }

        int expectedSize = PcmUtils.INSTANCE.bufferSize(10000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

        assertRiffHeader(dataInput, 0x80000000);
        assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);

        assertEquals(0x64617461, dataInput.readInt());
        dataInput.readInt();
        assertEquals(expectedSize, input.available());
    }

    @Test
    public void flushesBufferedSamplesOnClose() throws IOException {
        // I used to create multiple data chunks, each one having 5 seconds of data. Turns out
        // you're only meant to create a single data chunk. Unless I use Wave list Chunk,
        // but I won't.

        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream, 2000000);
        DataInputStream dataInput = new DataInputStream(input);

        final int sampleRate = 8000;
        final int bitsPerSample = 8;
        final int channels = 1;

        PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample);
        p2w.feed(generateSilence(6000, channels, sampleRate, bitsPerSample));

        int expectedSize = PcmUtils.INSTANCE.bufferSize(6000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

        assertRiffHeader(dataInput, 0x80000000);
        assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);

        assertEquals(0x64617461, dataInput.readInt());
        dataInput.readInt();

        assertNotEquals(expectedSize, input.available());
        p2w.close();
        assertEquals(expectedSize, input.available());
    }
}
