package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Test

class MessageRequestHintTest {
    @Test
    fun test_marshal() {
        val expected = MessageRequestHint(3)
        val expectedBytes = ubyteArrayOf(0x0f, 0x00, 0x06, 0x0b, 0xe4, 0x03)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        Assert.assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageRequestHint
        Assert.assertEquals(expected, msg)
    }
}
