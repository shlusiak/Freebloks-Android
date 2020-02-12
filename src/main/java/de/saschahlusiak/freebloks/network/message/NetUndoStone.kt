package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

class NetUndoStone: Message(MessageType.UndoStone) {
    companion object {
        fun from(data: ByteBuffer): NetUndoStone {
            // BUG: MSG_UNDO_STONE is actually a NET_SET_STONE with random payload; don't verify
            repeat(6) { data.get() }

            return NetUndoStone()
        }
    }
}