package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class MessageRequestUndoTest {
    @Test
    fun test_marshal() {
        val expected = MessageRequestUndo()
        val expectedBytes = ubyteArrayOf(0x0c, 0x00, 0x05, 0x09, 0xe3)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageRequestUndo
        Assert.assertEquals(expected, msg)
    }
}
