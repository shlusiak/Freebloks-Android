package de.saschahlusiak.freebloks.network

import android.util.Log
import androidx.annotation.CallSuper
import de.saschahlusiak.freebloks.network.message.*
import de.saschahlusiak.freebloks.utils.hexString
import java.io.Serializable
import java.nio.ByteBuffer

/**
 * A network message base class
 *
 * @param rawType the [MessageType]]
 * @param size size of the payload in bytes (excluding header)
 */
abstract class Message(rawType: Int, val size: Int = 0): Serializable {

    constructor(type: MessageType, size: Int = 0): this(type.rawValue, size)

    // header only depends on type and size, so it can be pre-created
    val header = Header(rawType, size + Header.HEADER_SIZE)

    /**
     * All classes must provide a way to marshal the payload into a buffer
     *
     * @param buffer the entire message into the buffer, including the header
     */
    @CallSuper
    open fun write(buffer: ByteBuffer) {
        header.write(buffer)
    }

    /**
     * Returns a [ByteArray] with the complete payload
     */
    fun toByteArray(): ByteArray {
        return ByteArray(header.size).apply {
            val buffer = ByteBuffer.wrap(this)
            write(buffer)
            assert(buffer.remaining() == 0) { "Message $header not fully written, remaining ${buffer.remaining()}" }
        }
    }

    /**
     * Marshals the message and returns a hex string
     */
    fun asHexString(separator: String = ", ") = toByteArray().hexString(separator)

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
                MessageType.RequestPlayer -> MessageRequestPlayer.from(buffer)
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
                MessageType.RequestGameMode -> MessageRequestGameMode.from(buffer)

                else -> {
                    Log.e(tag, "Unhandled message type: ${header.messageType}")
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