package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageGrantPlayerTest {
    @Test
    fun test_marshal() {
        val expected = MessageGrantPlayer(2)
        val expectedBytes = ubyteArrayOf(0x06, 0x00, 0x06, 0x02, 0xd2, 0x02)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageGrantPlayer
        assertEquals(expected, msg)
    }
}
