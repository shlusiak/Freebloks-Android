package de.saschahlusiak.freebloks.network

import android.util.Log
import de.saschahlusiak.freebloks.network.message.NetCurrentPlayer
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.nio.ByteBuffer

/**
 * A network message base class
 *
 * @param type the message type. See [Network]
 * @param size size of the payload in bytes (excluding header)
 */
abstract class Message(val type: Int, val size: Int) {
    // header only depends on type and size, so it can be pre-created
    val header = Header(type, size + Header.HEADER_SIZE)

    /**
     * All classes must provide a way to marshal the payload into a buffer
     *
     * @param buffer the entire message into the buffer, including the header
     */
    abstract fun write(buffer: ByteBuffer)

    /**
     * Returns a [ByteArray] with the complete payload
     */
    fun toByteArray(): ByteArray {
        return ByteArray(header.size).apply {
            write(ByteBuffer.wrap(this))
        }
    }

    /**
     * Marshals the message and returns a hex string
     */
    fun dumpAsHex(): String {
        val sb = StringBuffer()
        toByteArray().forEach {
            sb.append(String.format("0x%02x ", it.toUnsignedByte()))
        }
        return sb.toString()
    }

    companion object {
        @JvmStatic
        fun from(buffer: ByteBuffer): Message? {
            val header = Header.from(buffer)

            return when (header.type) {
//            Network.MSG_REQUEST_PLAYER -> NET_REQUEST_PLAYER(p)
//            Network.MSG_GRANT_PLAYER -> NET_GRANT_PLAYER(p)
                Network.MSG_CURRENT_PLAYER -> NetCurrentPlayer.from(buffer)
//            Network.MSG_SET_STONE -> NET_SET_STONE(p)
//            Network.MSG_START_GAME -> NET_START_GAME(p)
//            Network.MSG_GAME_FINISH -> NET_GAME_FINISH(p)
//            Network.MSG_SERVER_STATUS -> NET_SERVER_STATUS(p)
//            Network.MSG_CHAT -> NET_CHAT(p)
//            Network.MSG_REQUEST_UNDO -> NET_REQUEST_UNDO(p)
//            Network.MSG_UNDO_STONE -> NET_UNDO_STONE(p)
//            Network.MSG_REQUEST_HINT -> NET_REQUEST_HINT(p)
//            Network.MSG_STONE_HINT -> NET_SET_STONE(p)
//            Network.MSG_REVOKE_PLAYER -> NET_REVOKE_PLAYER(p)

                else -> {
                    Log.e(Network.tag, "Unhandled message type: ${header.type}")
                    null
                }
            }
        }
    }
}