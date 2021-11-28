package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.network.Message
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageSetStoneTest {
    @Test
    fun test_marshall() {
        val org = MessageSetStone(1, 4, Orientation(false, Rotation.Left), 4, 6)

        println("$org")
        println("bytes: ${org.asHexString()}")

        val bytes = org.toByteArray()
        val msg = Message.from(bytes) as MessageSetStone
        assertEquals(org, msg)

        assertEquals(1, msg.player)
        assertEquals(4, msg.shape)
        assertEquals(4, msg.x)
        assertEquals(6, msg.y)
        assertEquals(Orientation(false, Rotation.Left), msg.orientation)
    }
}