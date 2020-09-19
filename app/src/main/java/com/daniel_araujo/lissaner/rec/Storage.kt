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
     * Moves audio out of storage. Callback receives a chunk of audio. It is guaranteed to be called
     * at least once, even if the storage is empty. It may be called multiple times.
     */
    fun move(cb: (ByteBuffer) -> Unit)

    /**
     * How much is in storage.
     */
    fun size(): Int

    /**
     * Clears contents in storage.
     */
    fun clear()
}