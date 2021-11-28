package de.saschahlusiak.freebloks.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ByteBufferTest {
    @Test
    fun testByteBufferOf() {
        val b = byteBufferOf(1, 2, 3, 255)
        assertEquals(4, b.capacity())
        assertEquals(4, b.limit())
        assertEquals(0, b.position())

        assertEquals(1.toByte(), b.get())
        assertEquals(2.toByte(), b.get())
        assertEquals(3.toByte(), b.get())
        assertEquals(255.toByte(), b.get())
    }
}