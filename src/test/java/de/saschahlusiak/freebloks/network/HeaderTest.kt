package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.utils.byteBufferOf
import de.saschahlusiak.freebloks.utils.toUnsigned
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.ByteBuffer

class HeaderTest {
    @Test
    fun test_checksum() {
        var header = Header(5, 11)
        assertEquals(4, header.check1)
        assertEquals(215, header.check2)

        header = Header(17, 105)
        assertEquals(80, header.check1)
        assertEquals(151, header.check2)

        header = Header(8, 14)
        assertEquals(12, header.check1)
        assertEquals(226, header.check2)
    }

    @Test
    fun test_header_write_and_read() {
        val header = Header(4, 8)
        val buffer = ByteBuffer.allocate(100)

        header.write(buffer)
        assertEquals(Header.HEADER_SIZE, buffer.position())
        buffer.flip()

        val h2 = Header.from(buffer)
        assertNotNull(h2)
        assertEquals(header, h2)
    }

    @Test
    fun test_header_read() {
        val buffer = byteBufferOf(
            0x15, 0x01, 0x2C, 0x11, 0xD4
        )
        val header = Header.from(buffer)

        assertNotNull(header)
        assertEquals(17, header.type)
        assertEquals(300, header.size)
    }

    @Test
    fun test_header_write() {
        val header = Header(17, 300)
        val buffer = ByteBuffer.allocate(6)

        header.write(buffer)
        buffer.flip()

        assertEquals(0x15, buffer.get().toUnsigned())
        assertEquals(0x01, buffer.get().toInt())
        assertEquals(0x2C, buffer.get().toInt())
        assertEquals(0x11, buffer.get().toInt())
        assertEquals(0xD4, buffer.get().toUnsigned())
    }
}