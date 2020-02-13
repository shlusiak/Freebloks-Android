package de.saschahlusiak.freebloks.network

import android.util.Log
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.network.message.*
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.io.Serializable
import java.nio.ByteBuffer

/**
 * A network message base class
 *
 * @param type the [MessageType]]
 * @param size size of the payload in bytes (excluding header)
 */
abstract class Message(val type: MessageType, val size: Int = 0): Serializable {
    // header only depends on type and size, so it can be pre-created
    val header = Header(type.rawValue, size + Header.HEADER_SIZE)

    /**
     * All classes must provide a way to marshal the payload into a buffer
     *
     * @param buffer the entire message into the buffer, including the header
     */
    open fun write(buffer: ByteBuffer) {
        buffer.put(header)
    }

    /**
     * Returns a [ByteArray] with the complete payload
     */
    fun toByteArray(): ByteArray {
        return ByteArray(header.size).apply {
            write(ByteBuffer.wrap(this))
        }
    }

    /**
     * Returns a [ByteBuffer] wrapping the content
     */
    fun toBuffer() = ByteBuffer.wrap(toByteArray())

    /**
     * Marshals the message and returns a hex string
     */
    fun dumpAsHex(): String {
        val sb = StringBuffer()
        toByteArray().forEach {
            sb.append(String.format("0x%02x, ", it.toUnsignedByte()))
        }
        return sb.toString()
    }

    companion object {
        private val tag = Message::class.java.simpleName

        fun from(bytes: ByteArray) = from(ByteBuffer.wrap(bytes))

        fun from(buffer: ByteBuffer): Message? {
            val header = Header.from(buffer)

            val remaining = buffer.remaining()
            assert(buffer.remaining() == (header.size - Header.HEADER_SIZE)) {
                "Message to small, expected ${header.size - Header.HEADER_SIZE} but got $remaining"
            }

            val message = when (header.messageType) {
//                MessageType.RequestPlayer -> NET_REQUEST_PLAYER(p)
                MessageType.GrantPlayer -> MessageGrantPlayer.from(buffer)
                MessageType.CurrentPlayer -> MessageCurrentPlayer.from(buffer)
                MessageType.SetStone -> MessageSetStone.from(buffer)
                MessageType.StartGame -> MessageStartGame()
                MessageType.GameFinish -> MessageGameFinish()
                MessageType.ServerStatus -> MessageServerStatus.from(buffer)
                MessageType.Chat -> MessageChat.from(buffer)
                MessageType.RequestUndo -> MessageRequestUndo()
                MessageType.UndoStone -> MessageUndoStone.from(buffer)
                MessageType.RequestHint -> MessageRequestHint.from(buffer)
                MessageType.StoneHint -> MessageStoneHint.from(buffer)
                MessageType.RevokePlayer -> MessageRevokePlayer.from(buffer)

                else -> {
                    Log.e(tag, "Unhandled message type: ${header.messageType}")
                    if (BuildConfig.DEBUG) {
                        throw UnsupportedOperationException("Message type ${header.messageType} not implemented")
                    }

                    null
                }
            }
            assert(buffer.remaining() == 0) { "Buffer not fully consumed, remaining ${buffer.remaining()} of $header" }

            return message
        }

        @Throws(ProtocolException::class)
        internal fun assert(condition: Boolean, lazyMessage: (() -> (String))) {
            if (condition) return
            val message = lazyMessage.invoke()
            throw ProtocolException(message)
        }
    }
}