package com.daniel_araujo.always_recording_microphone

object PcmUtils {
    /**
     * Calculates the number of bytes needed to store samples.
     */
    fun bufferSize(milli: Int, sampleRate: Int, bytesPerSample: Int, channels: Int): Int {
        return milli * sampleRate * bytesPerSample * channels / 1000
    }
}