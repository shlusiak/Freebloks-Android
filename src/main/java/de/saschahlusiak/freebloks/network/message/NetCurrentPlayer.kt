package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

data class NetCurrentPlayer(val player: Int): Message(Network.MSG_CURRENT_PLAYER, 1) {
    init {
        assert(player >= -1 && player <= 3) { "Invalid player $player" }
    }

    override fun write(buffer: ByteBuffer) {
        buffer.put(header)
        buffer.put(player.toByte())
    }

    companion object {
        fun from(data: ByteBuffer): NetCurrentPlayer {
            assert(data.remaining() == 1) { "Payload size 1 expected but is ${data.remaining()}" }

            val player = data.get()
            return NetCurrentPlayer(player.toInt())
        }
    }
}