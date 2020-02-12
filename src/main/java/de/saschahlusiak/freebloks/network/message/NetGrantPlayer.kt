package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

data class NetGrantPlayer(val player: Int): Message(Network.MSG_GRANT_PLAYER, 1) {
    init {
        assert(player in 0..3) { "Invalid player $player" }
    }

    override fun write(buffer: ByteBuffer) {
        buffer.put(header)
        buffer.put(player.toByte())
    }

    companion object {
        fun from(data: ByteBuffer): NetGrantPlayer {
            val player = data.get().toInt()
            return NetGrantPlayer(player)
        }
    }
}