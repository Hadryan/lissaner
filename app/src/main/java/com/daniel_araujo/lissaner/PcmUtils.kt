package com.daniel_araujo.lissaner

import com.daniel_araujo.lissaner.android.Application
import com.daniel_araujo.lissaner.android.PreferenceUtils

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

    /**
     * Minimum amount of bytes needed to store a sample with the given bit depth.
     */
    fun bytesPerSample(bitsPerSample: Int): Int {
        return Math.ceil(bitsPerSample.toDouble() / 8).toInt()
    }
}