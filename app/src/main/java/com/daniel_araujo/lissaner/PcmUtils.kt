package com.daniel_araujo.lissaner

object PcmUtils {
    /**
     * Calculates the number of bytes needed to store samples.
     */
    fun bufferSize(milli: Long, sampleRate: Int, bytesPerSample: Int, channels: Int): Int {
        return (milli * sampleRate * bytesPerSample * channels / 1000).toInt()
    }

    /**
     * Calculates duration in milliseconds based off size of samples in bytes.
     */
    fun duration(size: Int, sampleRate: Int, bytesPerSample: Int, channels: Int): Long {
        val sizePerSecond = sampleRate * bytesPerSample * channels
        return size.toLong() * 1000 / sizePerSecond
    }
}