package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test


class MessageStartGameTest {
    @Test
    fun test_marshal() {
        val expected = MessageStartGame()
        val expectedBytes = ubyteArrayOf(0x00, 0x00, 0x05, 0x05, 0xdb)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageStartGame
        assertEquals(expected, msg)
    }
}
