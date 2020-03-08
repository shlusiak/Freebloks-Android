package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageChatTest {
    @Test
    fun test_marshal() {
        val expected = ubyteArrayOf(
            0x0d, 0x00, 0x0d, 0x08, 0xe3, 0x01, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x00
        )
        val msg1 = MessageChat(1, "Hello")
        val bytes = msg1.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expected, bytes)
        val msg2 = Message.from(bytes) as MessageChat

        assertEquals(msg1, msg2)
        assertEquals(1, msg2.client)
        assertEquals("Hello", msg2.message)
    }

    @Test
    fun test_marshal_empty() {
        val expected = ubyteArrayOf(
            0x08, 0x00, 0x08, 0x08, 0xe6, 0x03, 0x00, 0x00
        )
        val msg1 = MessageChat(3, "")
        val bytes = msg1.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expected, bytes)

        val msg2 = Message.from(bytes) as MessageChat

        assertEquals(msg1, msg2)
        assertEquals(3, msg2.client)
        assertEquals("", msg2.message)
    }

    @Test
    fun test_marshal_trim() {
        val msg1 = MessageChat(-1, "hey \n")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageChat

        assertEquals(-1, msg2.client)
        assertEquals("hey", msg2.message)
    }

    @Test
    fun test_marshal_utf8() {
        val msg1 = MessageChat(3, "hey Ääßéん")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageChat

        assertEquals(3, msg2.client)
        assertEquals("hey Ääßéん", msg2.message)
    }
}