package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.network.*
import java.nio.ByteBuffer

class MessageStartGame : Message(MessageType.StartGame) {
    override fun equals(other: Any?) = other is MessageStartGame
    override fun hashCode() = 0
}