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
}