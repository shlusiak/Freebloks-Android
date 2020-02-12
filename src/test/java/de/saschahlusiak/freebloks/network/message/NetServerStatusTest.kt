package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.controller.GameMode
import de.saschahlusiak.freebloks.controller.Spielleiter.PLAYER_COMPUTER
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer

class NetServerStatusTest {
    @Test
    fun test_unmarshall() {
        val bytes = ubyteArrayOf(
            0x06, 0x00, 0xab, 0x07, 0xd7, 0x00, 0x04, 0x01, 0x14, 0x14, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 0xfe, 0xfe, 0xfe, 0xfe, 0x00, 0xae, 0x62, 0xc4, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xb8, 0xab, 0x50, 0xbb, 0x00, 0x0b, 0x15, 0xee, 0x04, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x40, 0xc4, 0xf4, 0xac,
            0x85, 0xd8, 0x00, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x00, 0x00, 0x40, 0x91, 0x22, 0xee, 0x01, 0x00, 0x00, 0x00, 0x00, 0xac, 0x50, 0xbb, 0x38, 0xac, 0x50,
            0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xab, 0xa3, 0xa0, 0xa7, 0x00, 0x91, 0x22, 0xee, 0x46, 0xac, 0x50, 0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xe8, 0xab, 0x50, 0xbb,
            0x00, 0xae, 0x62, 0xc4, 0x18, 0x00, 0x00, 0x00, 0x34, 0x7c, 0x22, 0xee, 0xe8, 0xab, 0x50, 0xbb, 0x00, 0x83, 0x14, 0xee, 0xa0, 0xae, 0x62, 0xc4, 0x08,
            0x02, 0xff, 0xff, 0xf8, 0xab, 0x50, 0xbb, 0x00, 0x84, 0x14, 0xee, 0xff, 0x25, 0x19, 0xee, 0xc4, 0xfd, 0x51, 0xbb, 0x28, 0xac, 0x50, 0xbb, 0x03, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01
        )

        val msg = Message.from(ByteBuffer.wrap(bytes)) as NetServerStatus
        assertNotNull(msg)

        assertEquals(0, msg.player)
        assertEquals(4, msg.computer)
        assertEquals(1, msg.clients)
        assertEquals(20, msg.width)
        assertEquals(20, msg.height)
        assertEquals(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, msg.gameMode)
        assertEquals(PLAYER_COMPUTER, msg.spieler[0])
        assertEquals(PLAYER_COMPUTER, msg.spieler[1])
        assertEquals(PLAYER_COMPUTER, msg.spieler[2])
        assertEquals(PLAYER_COMPUTER, msg.spieler[3])
        assertNull(msg.clientNames[0])
        assertEquals(3, msg.version)
        assertEquals(1, msg.minVersion)
        for (i in 0 until 21) {
            assertEquals(1, msg.stoneNumbers[i])
        }

        // above data contains rubbish that isn't binary identical when marshaled
        // so just marshal and unmarshal it and compare that for equality
        val marshaled = msg.toByteArray()
        val unmarshalled = Message.from(ByteBuffer.wrap(marshaled))
        assertEquals(msg, unmarshalled)
    }

    @Test
    fun test_marshal() {
        val msg = NetServerStatus(
            1, 3, 4,
            15, 15, GameMode.GAMEMODE_DUO,
            intArrayOf(1, 2, 3, 4),
            arrayOf("Paul", "Peter", null, "Nobody", null, null, null, "x"),
            3,
            1,
            IntArray(21) { it }
        )

        val bytes = msg.toByteArray()
        val copy = Message.from(bytes) as NetServerStatus

        assertEquals(msg, copy)

        assertEquals("Paul", copy.getClientName(null, 0))
        assertEquals("Peter", copy.getClientName(null, 1))
        assertEquals("Client 2", copy.getClientName(null, 2))
        assertEquals("Nobody", copy.getClientName(null, 3))
        assertEquals("Client 4", copy.getClientName(null, 4))
        assertEquals("Client 5", copy.getClientName(null, 5))
        assertEquals("Client 6", copy.getClientName(null, 6))
        assertEquals("x", copy.getClientName(null, 7))
    }

}