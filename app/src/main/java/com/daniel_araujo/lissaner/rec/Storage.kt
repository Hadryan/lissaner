package com.daniel_araujo.lissaner.rec

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

    /**
     * Moves audio out of storage.
     */
    fun move(): ByteArray

    /**
     * How much is in storage.
     */
    fun size(): Int
}