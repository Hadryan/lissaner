package com.daniel_araujo.lissaner.rec

import com.daniel_araujo.lissaner.ByteRingBuffer
import java.nio.ByteBuffer

/**
 * Stores audio samples in memory as is.
 */
class PureMemoryStorage : Storage {
    private val buf: ByteRingBuffer

    constructor(size: Int) {
        buf = ByteRingBuffer(size)
    }

    override fun feed(buffer: ByteBuffer) {
        val array = buffer.array()

        buf.add(array, buffer.arrayOffset(), buffer.limit())

        // Consumed.
        buffer.rewind()
    }

    override fun copy(): ByteArray {
        val result = ByteArray(buf.size)

        buf.peek(result)

        return result
    }

    override fun move(cb: (ByteBuffer) -> Unit) {
        buf.peek(cb)
        buf.clear()
    }

    override fun size(): Int {
        return buf.size
    }

    override fun clear() {
        buf.clear()
    }
}