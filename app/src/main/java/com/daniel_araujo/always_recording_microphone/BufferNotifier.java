package com.daniel_araujo.always_recording_microphone;

import java.util.Arrays;

/**
 * Accumulates data until it reaches a threshold.
 */
public class BufferNotifier implements AutoCloseable {
    @Override
    public void close() throws Exception {

    }

    public interface OnThresholdListener {
        /**
         * Receives samples. The samples array will be reused after the method is called so make
         * sure to create a copy if you're going to use it afterwards.
         *
         * @param buffer
         */
        void onThreshold(byte[] buffer);
    }

    private OnThresholdListener onThresholdListener = null;

    /**
     * Buffer.
     */
    private final byte[] buffer;

    /**
     * How many bytes have been accumulated in buffer.
     */
    private int accumulated;

    /**
     * Buffer threshold in bytes.
     */
    private final int threshold;

    public BufferNotifier(int threshold) {
        this.threshold = threshold;

        buffer = new byte[threshold];
    }

    /**
     * Sets listener for samples.
     *
     * @param listener
     */
    public void setOnThresholdListener(OnThresholdListener listener) {
        onThresholdListener = listener;
    }

    /**
     * Adds a chunk of bytes to the buffer. Calls listeners if threshold is hit.
     *
     * @param chunk
     */
    public void add(byte[] chunk) {
        int offset = 0;
        int size = chunk.length;

        while (size > 0) {
            int remaining = buffer.length - accumulated;

            if (size >= remaining) {
                // Current buffer will be filled up
                System.arraycopy(chunk, offset, buffer, accumulated, remaining);

                if (onThresholdListener != null) {
                    onThresholdListener.onThreshold(buffer);
                }

                accumulated = 0;

                offset += remaining;
                size -= remaining;
            } else {
                // Current buffer will be partially filled up.
                System.arraycopy(chunk, offset, buffer, accumulated, size);

                accumulated += size;

                offset += size;
                size -= size;
            }
        }
    }

    /**
     * Creates a copy of the current contents of the buffer.
     * @return
     */
    public byte[] copy() {
        return Arrays.copyOfRange(buffer, 0, accumulated);
    }
}
