package com.daniel_araujo.always_recording_microphone.rec

import org.junit.Test

import org.junit.Assert.*
import java.nio.ByteBuffer

class PureMemoryStorageTest {
    @Test
    fun feedItSize() {
        val f = PureMemoryStorage(4)
        
        val data = byteArrayOf(1, 2, 3, 4)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(data, f.copy())
        assertEquals(4, f.size())
    }

    @Test
    fun feedItsSizeAndHalf() {
        val f = PureMemoryStorage(4)
        
        val data = byteArrayOf(1, 2, 3, 4, 5, 6)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(byteArrayOf(3, 4, 5, 6), f.copy())
        assertEquals(4, f.size())
    }

    @Test
    fun feedTwiceItsSize() {
        val f = PureMemoryStorage(4)
        
        val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(byteArrayOf(9, 10, 11, 12), f.copy())
        assertEquals(4, f.size())
    }

    @Test
    fun feedThriceItsSize() {
        val f = PureMemoryStorage(4)
        
        val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(byteArrayOf(9, 10, 11, 12), f.copy())
        assertEquals(4, f.size())
    }

    @Test
    fun feedingMoreDataThanItsSizeShouldDiscardOlderValues() {
        val f = PureMemoryStorage(4)

        val data = byteArrayOf(1, 2, 3, 4)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        val data2 = byteArrayOf(5, 6)

        val buffer2 = ByteBuffer.wrap(data2)
        buffer2.position(buffer2.limit())
        f.feed(buffer2)

        assertArrayEquals(byteArrayOf(3, 4, 5, 6), f.copy())
        assertEquals(4, f.size())
    }

    @Test
    fun feedingLessThanItsSizeShouldReturnOnlyThoseElements() {
        val f = PureMemoryStorage(4)

        val data = byteArrayOf(1, 2)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(byteArrayOf(1, 2), f.copy())
        assertEquals(2, f.size())
    }

    @Test
    fun move_clearsStorage() {
        val f = PureMemoryStorage(4)

        val data = byteArrayOf(1, 2)

        val buffer = ByteBuffer.wrap(data)
        buffer.position(buffer.limit())
        f.feed(buffer)

        assertArrayEquals(byteArrayOf(1, 2), f.move())
        assertEquals(0, f.size())
    }
}