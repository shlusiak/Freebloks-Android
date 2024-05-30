package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*

class MessageRequestUndo: Message(MessageType.RequestUndo, 0) {
    override fun equals(other: Any?) = other is MessageRequestUndo
    override fun hashCode() = 0
    override fun toString() = "MessageRequestUndo"
}