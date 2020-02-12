package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

class MessageGameFinish : Message(MessageType.GameFinish) {
    override fun equals(other: Any?) = other is MessageGameFinish
    override fun hashCode() = 0
}