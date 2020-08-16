package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

// BUG: MSG_UNDO_STONE is actually a NET_SET_STONE with random payload; don't verify
class MessageUndoStone: Message(MessageType.UndoStone, 6) {
    override fun equals(other: Any?) = other is MessageUndoStone
    override fun hashCode() = 0

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        repeat(6) { buffer.put(0) }
    }

    companion object {
        fun from(data: ByteBuffer): MessageUndoStone {
            repeat(6) { data.get() }

            return MessageUndoStone()
        }
    }
}