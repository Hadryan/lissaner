package com.daniel_araujo.always_recording_microphone.rec

import com.daniel_araujo.always_recording_microphone.ByteRingBuffer
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

    override fun move(): ByteArray {
        val result = ByteArray(buf.size)

        buf.pop(result)

        return result
    }

    override fun size(): Int {
        return buf.size
    }
}