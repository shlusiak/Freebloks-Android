package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class NetChatTest {
    @Test
    fun test_marshal() {
        val msg1 = NetChat(1, "Hello")
        val bytes = msg1.toByteArray()
        val msg2 = Message.from(bytes) as NetChat

        assertEquals(msg1, msg2)
        assertEquals(1, msg2.client)
        assertEquals("Hello", msg2.message)
    }
}