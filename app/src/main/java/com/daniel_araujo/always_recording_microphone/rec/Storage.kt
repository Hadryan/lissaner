package com.daniel_araujo.always_recording_microphone.rec

import java.nio.ByteBuffer

interface Storage {
    /**
     * Feeds data
     */
    fun feed(buffer: ByteBuffer)

    /**
     * Returns a copy of the current buffer.
     */
    fun copy(): ByteArray
}