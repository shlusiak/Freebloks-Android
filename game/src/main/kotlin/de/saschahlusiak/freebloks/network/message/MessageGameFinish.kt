package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*

class MessageGameFinish : Message(MessageType.GameFinish) {
    override fun equals(other: Any?) = other is MessageGameFinish
    override fun hashCode() = 0
    override fun toString() = "MessageGameFinish"
}