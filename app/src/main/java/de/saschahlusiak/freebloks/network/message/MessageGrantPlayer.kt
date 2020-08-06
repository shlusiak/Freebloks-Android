package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

data class MessageGrantPlayer(val player: Int): Message(MessageType.GrantPlayer, 1) {
    init {
        assert(player in 0..3) { "Invalid player $player" }
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(player.toByte())
    }

    companion object {
        fun from(data: ByteBuffer): MessageGrantPlayer {
            val player = data.get().toInt()
            return MessageGrantPlayer(player)
        }
    }
}