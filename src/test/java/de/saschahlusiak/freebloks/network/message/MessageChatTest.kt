package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.*
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
    fun test_marshal_utf8_legacy() {
        // this is how utf8 was marshaled before, as plain bytes for each char
        val bytes = ubyteArrayOf(0x19, 0x00, 0x11, 0x08, 0xd7, 0x03, 0x09, 0x68, 0x65, 0x79, 0x20, 0xc4, 0xe4, 0xdf, 0xe9, 0x93, 0x00)
        val msg = Message.from(bytes) as MessageChat
        assertNotNull(msg)

        assertEquals(3, msg.client)
        assertEquals(8, msg.message.length)
        assertEquals("hey ����", msg.message)
    }

    @Test
    fun test_marshal_utf8() {
        val msg1 = MessageChat(3, "hey Ääßéん")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageChat

        println(msg1.asHexString())

        assertEquals(3, msg2.client)
        assertEquals("hey Ääßéん", msg2.message)
    }
}