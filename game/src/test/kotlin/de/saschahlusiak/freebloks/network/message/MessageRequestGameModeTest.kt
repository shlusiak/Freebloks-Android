package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageType
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.nio.ByteBuffer


class MessageRequestGameModeTest {
    @Test
    fun test_marshal() {
        val expected = MessageRequestGameMode(
            19, 21, GameMode.GAMEMODE_JUNIOR, IntArray(21) { 2 }
        )
        val expectedBytes = ubyteArrayOf(0x1a, 0x00, 0x1e, 0x0e, 0xda, 0x01, 0x13, 0x15, 0x04, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02)

        val bytes = expected.toByteArray()
        println(bytes.hexString())

        assertArrayEquals(expectedBytes, bytes)

        val msg = Message.from(bytes) as MessageRequestGameMode
        Assert.assertEquals(expected, msg)
    }
}
