package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer


class MessageUndoStoneTest {
    @Test
    fun test_marshal() {
        val expected = MessageUndoStone()
        val expectedBytes = ubyteArrayOf(0x0b, 0x00, 0x0b, 0x0a, 0xe7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageUndoStone
        assertEquals(expected, msg)
    }
}
