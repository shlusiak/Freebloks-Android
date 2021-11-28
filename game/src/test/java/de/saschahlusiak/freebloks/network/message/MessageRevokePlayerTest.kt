package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer


class MessageRevokePlayerTest {
    @Test
    fun test_marshall() {
        val bytes = ubyteArrayOf(0x09, 0x00, 0x06, 0x0d, 0xec, 0x03)
        val msg = MessageRevokePlayer(3)

        val newBytes = msg.toByteArray()
        println(newBytes.hexString())
        Assert.assertArrayEquals(bytes, newBytes)

        val msg2 = Message.from(bytes) as MessageRevokePlayer
        Assert.assertEquals(msg, msg2)
        Assert.assertEquals(3, msg2.player)
    }
}
