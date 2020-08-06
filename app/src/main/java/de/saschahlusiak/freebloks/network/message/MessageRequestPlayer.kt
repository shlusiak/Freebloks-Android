package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.put
import java.nio.ByteBuffer
import kotlin.math.min

data class MessageRequestPlayer(val player: Int, val name: String?): Message(MessageType.RequestPlayer, 17) {
    init {
        assert(player in -1..3) { "player $player must be between -1 and 3"}
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(player.toByte())
        val bytes = name?.toByteArray() ?: ByteArray(0)
        buffer.put(bytes, 15).put(0)
    }

    companion object {
        fun from(data: ByteBuffer): MessageRequestPlayer {
            val player = data.get().toInt()
            val bytes = ByteArray(16) { data.get() }
            val length = bytes.indexOfFirst { it == 0.toByte() }
            val name = if (length < 0) String(bytes, Charsets.UTF_8) else String(bytes, 0, length, Charsets.UTF_8)

            return MessageRequestPlayer(player, name.trimEnd().ifEmpty { null })
        }
    }
}