package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageCurrentPlayerTest {
    @Test
    fun test_marshall() {
        val bytes = ubyteArrayOf(0x07, 0x00, 0x06, 0x03, 0xd4, 0x03)
        val msg = MessageCurrentPlayer(3)

        val newBytes = msg.toByteArray()
        println(newBytes.hexString())
        assertArrayEquals(bytes, newBytes)

        val msg2 = Message.from(bytes) as MessageCurrentPlayer
        assertEquals(msg, msg2)
        assertEquals(3, msg2.player)
    }
}
