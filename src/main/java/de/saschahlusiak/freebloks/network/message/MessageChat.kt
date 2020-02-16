package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.forRemaining
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.nio.ByteBuffer

data class MessageChat(val client: Int, val message: String): Message(MessageType.Chat, message.length + 3) {
    init {
        assert(message.length < 255) { "message exceeds limit of 255 characters" }
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(client.toByte())
        buffer.put(message.length.toByte())
        message.forEach { buffer.put(it.toByte()) }
        buffer.put(0)
    }

    companion object {
        fun from(data: ByteBuffer): MessageChat {
            val client = data.get().toInt()
            val length = data.get().toUnsignedByte()
            val bytes = ByteArray(length) { data.get() }

//          consume all the rest, because we don't always only have a trailing 0
            data.forRemaining {
                if (it.toInt() != 0) println("Ignore excess byte ${it.toUnsignedByte()}")
            }

            return MessageChat(client, String(bytes).trimEnd())
        }
    }
}