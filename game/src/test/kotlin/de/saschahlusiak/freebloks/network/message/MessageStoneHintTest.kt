package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer

class MessageStoneHintTest {
    @Test
    fun test_marshal() {
        val expected = MessageStoneHint(2, 3, true, Rotation.Half, 4, 7)
        val expectedBytes = ubyteArrayOf(0x0d, 0x00, 0x0b, 0x0c, 0xe7, 0x02, 0x03, 0x01, 0x02, 0x04, 0x07)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageStoneHint
        assertEquals(expected, msg)

        assertEquals(2, msg.player)
        assertEquals(3, msg.shape)
        assertEquals(Rotation.Half, msg.rotation)
        assertEquals(4, msg.x)
        assertEquals(7, msg.y)
    }
}
