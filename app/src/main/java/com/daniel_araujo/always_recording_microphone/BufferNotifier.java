package com.daniel_araujo.always_recording_microphone;

import java.util.ArrayList;
import java.util.List;

/**
 * Accumulates samples until it reaches a threshold.
 */
public class BufferNotifier {
    public interface OnSamplesListener {
        /**
         * Receives samples. The samples array will be reused after the method is called so make
         * sure to create a copy if you're going to use it afterwards.
         *
         * @param samples
         */
        void onSamples(byte[] samples);
    }

    private OnSamplesListener onSamplesListener = null;

    /**
     * Accumulated samples.
     */
    private final byte[] buffer;

    /**
     * How many samples have been accumulated in bytes.
     */
    private int accumulated;

    /**
     * Buffer threshold in milliseconds.
     */
    private final long threshold;

    /**
     * Number of samples in a second.
     */
    private final int sampleRate;

    /**
     * Bytes per sample.
     */
    private final int bytesPerSample;

    public BufferNotifier(long threshold, int sampleRate, int bytesPerSample) {
        this.threshold = threshold;
        this.sampleRate = sampleRate;
        this.bytesPerSample = bytesPerSample;

        buffer = new byte[bufferSize()];
    }

    /**
     * Sets listener for samples.
     *
     * @param listener
     */
    public void setOnSamplesListener(OnSamplesListener listener) {
        onSamplesListener = listener;
    }

    /**
     * Adds samples to the buffer.
     *
     * @param samples
     */
    public void add(byte[] samples) {
        int offset = 0;
        int size = samples.length;

        while (size > 0) {
            int remaining = buffer.length - accumulated;

            if (size >= remaining) {
                // Current buffer will be filled up
                System.arraycopy(samples, offset, buffer, accumulated, remaining);

                if (onSamplesListener != null) {
                    onSamplesListener.onSamples(samples);
                }

                accumulated = 0;

                offset += remaining;
                size -= remaining;
            } else {
                // Current buffer will be partially filled up.
                System.arraycopy(samples, offset, buffer, accumulated, size);

                accumulated += size;

                offset += size;
                size -= size;
            }
        }
    }

    /**
     * @return Size of entire buffer.
     */
    private int bufferSize() {
        return (int) (threshold * sampleRate * bytesPerSample / 1000);
    }
}
