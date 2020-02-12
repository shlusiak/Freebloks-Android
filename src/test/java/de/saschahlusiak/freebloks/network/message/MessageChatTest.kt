package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageChatTest {
    @Test
    fun test_marshal() {
        val msg1 = MessageChat(1, "Hello")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as MessageChat

        assertEquals(msg1, msg2)
        assertEquals(1, msg2.client)
        assertEquals("Hello", msg2.message)
    }
}