package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MessageRequestPlayerTest {
    @Test
    fun test_marshal() {
        val msg1 = MessageRequestPlayer(3, "Hello")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageRequestPlayer

        assertEquals(msg1, msg2)
        assertEquals(3, msg2.player)
        assertEquals("Hello", msg2.name)
    }

    @Test
    fun test_marshal_null() {
        val msg1 = MessageRequestPlayer(-1, null)
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageRequestPlayer

        assertEquals(msg1, msg2)
        assertEquals(-1, msg2.player)
        assertNull(msg2.name)
    }

    @Test
    fun test_marshal_longName() {
        // we only have space for 16 chars, so leave one for the null byte and we expect it to be cut at 15 chars
        var org = MessageRequestPlayer(2, "12345678901234567")
        var msg = Message.from(org.toByteArray()) as MessageRequestPlayer
        assertEquals("123456789012345", msg.name)

        org = MessageRequestPlayer(2, "1234567890123456")
        msg = Message.from(org.toByteArray()) as MessageRequestPlayer
        assertEquals("123456789012345", msg.name)

        org = MessageRequestPlayer(2, "123456789012345")
        msg = Message.from(org.toByteArray()) as MessageRequestPlayer
        assertEquals("123456789012345", msg.name)

        org = MessageRequestPlayer(2, "12345678901234")
        msg = Message.from(org.toByteArray()) as MessageRequestPlayer
        assertEquals("12345678901234", msg.name)
    }
}