package com.daniel_araujo.always_recording_microphone;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PCM2WAV implements AutoCloseable {
    /**
     * Number of channels.
     */
    int channels;

    /**
     * Sampling rate.
     */
    int sampleRate;

    /**
     * Bits per sample for one channel.
     */
    int bitsPerSample;

    /**
     * The stream where the wav file is gradually constructed. Write raw bytes here.
     */
    private OutputStream writer;

    /**
     * Convenient wrapper for the wav file stream. Allows you to write Java data types in big endian.
     */
    private DataOutputStream dataWriter;

    public PCM2WAV(OutputStream stream, int channels, int sampleRate, int bitsPerSample) {
        if (channels <= 0) {
            throw new PCM2WAVException("You must have at least 1 channel.");
        } else if (channels > Short.MAX_VALUE) {
            // The constructor parameter is int out of convenience. It also allows me to be funny.
            throw new PCM2WAVException("Due to limitations in the WAVE format, the number of channels must fit in a 2 byte unsigned integer. Sorry for the inconvenience.");
        }

        if (sampleRate <= 0) {
            throw new PCM2WAVException("Sample rate must be a positive number.");
        }

        if (bitsPerSample <= 0) {
            throw new PCM2WAVException("Bytes per sample must be a positive number.");
        }

        this.channels = channels;

        this.sampleRate = sampleRate;

        this.bitsPerSample = bitsPerSample;

        writer = stream;
        dataWriter =  new DataOutputStream(writer);

        try {
            writeRiff();
            writeFmt();
        } catch (IOException ex) {
            throw new PCM2WAVException("Failed to generate header.", ex);
        }
    }

    /**
     * Feeds audio samples.
     * @param samples
     */
    public void feed(byte[] samples) {
        try {
            writeData(samples);
        } catch (IOException ex) {
            throw new PCM2WAVException("Failed to write data.", ex);
        }
    }

    /**
     * Disposes resources and writes remaining samples to the stream.
     */
    public void close() {
        // TODO
    }

    /**
     * The sampling rate you provided in the constructor. You're welcome.
     * @return
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * The number of channels you provided in the constructor. You're welcome.
     * @return
     */
    public int getChannels() {
        return channels;
    }

    /**
     * The number of bits per sample you provided in the constructor. You're welcome.
     * @return
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * How many bytes a sample occupies.
     * @return
     */
    public int getBytesPerSample() {
        return (int) Math.ceil((double) bitsPerSample / 8);
    }

    /**
     * Bytes per second. Multiply this by 8 to get bits per second.
     * @return
     */
    public int getBytesPerSecond() {
        return sampleRate * channels * getBytesPerSample();
    }

    /**
     * Writes the part that identifies the file format.
     * @throws IOException
     */
    private void writeRiff() throws IOException {
        // ChunkID. Always RIFF.
        writer.write(new byte[] { 'R', 'I', 'F', 'F' });

        // ChunkSize. The number of bytes that follow. Since we're constructing the file as we go,
        // we cannot guess the final size so I put a really huge value, just like I saw the arecord
        // command doing
        dataWriter.writeInt(Integer.reverseBytes(0x80000000));

        // Format. Always WAVE.
        writer.write(new byte[] { 'W', 'A', 'V', 'E' });
    }

    /**
     * Writes the part that describes the format such as number of channels, sampling rate, etc.
     * @throws IOException
     */
    private void writeFmt() throws IOException {
        // Subchunk1ID.
        writer.write(new byte[] { 'f', 'm', 't', ' ' });

        // Subchunk1Size. It is always 16 for PCM.
        dataWriter.writeInt(Integer.reverseBytes(16));

        // AudioFormat. 1 for PCM.
        dataWriter.writeShort(Short.reverseBytes((short) 1));

        // NumChannels.
        dataWriter.writeShort(Short.reverseBytes((short) channels));

        // SampleRate.
        dataWriter.writeInt(Integer.reverseBytes(sampleRate));

        // ByteRate. Basically the amount of samples in bytes per second.
        dataWriter.writeInt(Integer.reverseBytes(getBytesPerSecond()));

        // BlockAlign. The number of bytes for one sample in all channels.
        dataWriter.writeShort(Short.reverseBytes((short) (channels * getBytesPerSample())));

        // BitsPerSample.
        dataWriter.writeShort(Short.reverseBytes((short) bitsPerSample));
    }

    /**
     * Writes a chunk of audio.
     * @throws IOException
     */
    private void writeData(byte[] samples) throws IOException {
        // Subchunk2ID.
        writer.write(new byte[] { 'd', 'a', 't', 'a' });
        dataWriter.writeInt(Integer.reverseBytes(samples.length));
        writer.write(samples);
    }
}

class PCM2WAVException extends RuntimeException {
    public PCM2WAVException(String mes) {
        super(mes);
    }

    public PCM2WAVException(String mes, Throwable err) {
        super(mes, err);
    }
}