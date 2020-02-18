package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.lang.Integer.min
import java.nio.ByteBuffer

data class MessageRequestPlayer(val player: Int, val name: String?): Message(MessageType.RequestPlayer, 17) {
    init {
        assert(player in -1..3) { "player $player must be between -1 and 3"}
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(player.toByte())
        val name = name?.let { it.substring(0, min(it.length, 15)) } ?: ""
        name.forEach { buffer.put(it.toByte()) }
        repeat(16 - name.length) { buffer.put(0) }
    }

    companion object {
        fun from(data: ByteBuffer): MessageRequestPlayer {
            val player = data.get().toInt()
            val bytes = ByteArray(16) { data.get() }
            val length = bytes.indexOfFirst { it == 0.toByte() }
            val name = if (length < 0) String(bytes) else String(bytes, 0, length)

            return MessageRequestPlayer(player, name.trimEnd().ifEmpty { null })
        }
    }
}