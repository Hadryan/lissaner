package com.daniel_araujo.always_recording_microphone.rec

import java.nio.ByteBuffer
import org.apache.commons.collections4.queue.CircularFifoQueue

/**
 * Stores audio samples in memory as is.
 */
class PureMemoryStorage : Storage {
    private lateinit var fifo: CircularFifoQueue<Byte>

    constructor(size: Int) {
        fifo = CircularFifoQueue(size)
    }

    override fun feed(buffer: ByteBuffer) {
        for (i in 0 until buffer.position()) {
            fifo.add(buffer.get(i))
        }

        // Consumed.
        buffer.rewind()
    }

    override fun copy(): ByteArray {
        val result = ByteArray(fifo.size)

        for (i in 0 until fifo.size) {
            result[i] = fifo[i]
        }

        return result
    }

    override fun size(): Int {
        return fifo.size
    }
}