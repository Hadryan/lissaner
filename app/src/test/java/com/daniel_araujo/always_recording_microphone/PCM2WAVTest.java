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
        public void expectedFmt() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
            }

            assertRiffHeader(dataInput, UNKNOWN_DATA_SIZE + OFFSET_TO_DATA);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
        }

        @Test
        public void usesUnknownDataSizeIfSizeIsNotSpecified() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, UNKNOWN_DATA_SIZE + OFFSET_TO_DATA);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
            assertDataChunkWithoutSamples(dataInput, UNKNOWN_DATA_SIZE);
        }

        @Test
        public void usesSpecifiedDataSize() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            final int expectedSize = PcmUtils.INSTANCE.bufferSize(5000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.expectSize(expectedSize);
                p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, expectedSize + OFFSET_TO_DATA);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
            assertDataChunkWithoutSamples(dataInput, expectedSize);
        }

        @Test
        public void writesExpectedAmountOfSamples() throws IOException {
            PipedOutputStream stream = new PipedOutputStream();
            PipedInputStream input = new PipedInputStream(stream, 2000000);
            DataInputStream dataInput = new DataInputStream(input);

            try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
                p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            }

            assertRiffHeader(dataInput, UNKNOWN_DATA_SIZE + OFFSET_TO_DATA);
            assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
            assertDataChunkWithoutSamples(dataInput, UNKNOWN_DATA_SIZE);

            int expectedSize = PcmUtils.INSTANCE.bufferSize(5000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

            assertEquals(expectedSize, input.available());
        }
    }

    /**
     * The size that is used when data size is not explicitly set.
     */
    private static final int UNKNOWN_DATA_SIZE = 0x80000000;

    /**
     * The offset from chunk size to the byte of the first sample.
     */
    private static final int OFFSET_TO_DATA = 36;

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

    private static void assertDataChunkWithoutSamples(DataInputStream dataInput, int expectedSize) throws IOException {
        // Subchunk2ID
        assertEquals(0x64617461, dataInput.readInt());

        // Subchunk2Size
        assertEquals(expectedSize, Integer.reverseBytes(dataInput.readInt()));
    }

    private static int calcBytesPerSample(int bitsPerSample) {
        return (int) Math.ceil((double) bitsPerSample / 8);
    }

    private static byte[] generateSilence(long duration, int channels, int sampleRate, int bitsPerSample) {
        byte[] buffer = new byte[(int) (duration * channels * sampleRate * calcBytesPerSample(bitsPerSample) / 1000)];

        return buffer;
    }

    @Test
    public void close_noDataWritesRiffFmtAndEmptyDataChunk() throws IOException {
        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream);
        DataInputStream dataInput = new DataInputStream(input);

        PCM2WAV p2w = new PCM2WAV(stream, 1, 44100, 16);

        assertEquals(0, input.available());

        p2w.close();

        assertNotEquals(0, input.available());

        assertRiffHeader(dataInput, UNKNOWN_DATA_SIZE + OFFSET_TO_DATA);
        assertFmtChunk(dataInput, 1, 44100, 16);
        assertDataChunkWithoutSamples(dataInput, UNKNOWN_DATA_SIZE);
        assertEquals(0, input.available());
    }

    @Test
    public void feed_multipleCallsWriteToASingleDataChunk() throws IOException {
        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream, 2000000);
        DataInputStream dataInput = new DataInputStream(input);

        final int sampleRate = 8000;
        final int bitsPerSample = 8;
        final int channels = 1;

        try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
            p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
        }

        int expectedSize = PcmUtils.INSTANCE.bufferSize(10000, sampleRate, calcBytesPerSample(bitsPerSample), channels);

        assertRiffHeader(dataInput, UNKNOWN_DATA_SIZE + OFFSET_TO_DATA);
        assertFmtChunk(dataInput, channels, sampleRate, bitsPerSample);
        assertDataChunkWithoutSamples(dataInput, UNKNOWN_DATA_SIZE);
        assertEquals(expectedSize, input.available());
    }

    @Test(expected = RuntimeException.class)
    public void expectSize_throwsErrorIfChunksAlreadyWritten() throws IOException {
        PipedOutputStream stream = new PipedOutputStream();
        PipedInputStream input = new PipedInputStream(stream, 2000000);
        DataInputStream dataInput = new DataInputStream(input);

        final int sampleRate = 8000;
        final int bitsPerSample = 8;
        final int channels = 1;

        try (PCM2WAV p2w = new PCM2WAV(stream, channels, sampleRate, bitsPerSample)) {
            p2w.feed(generateSilence(5000, channels, sampleRate, bitsPerSample));
            p2w.expectSize(1);
        }
    }
}
