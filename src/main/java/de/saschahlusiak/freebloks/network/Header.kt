package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.nio.ByteBuffer

fun ByteBuffer.put(header: Header) = header.write(this)

/**
 * check1 uint8
 * data_length uint16
 * msg_type uint8
 * check2 uint8
 */
data class Header(val rawType: Int, val size: Int) {
    constructor(type: MessageType, size: Int): this(type.rawValue, size)

    val check1 = ((size and 0x0055) xor rawType)

    val check2 = (check1 xor 0xD6) + rawType

    val messageType = MessageType.from(rawType)

    fun write(buffer: ByteBuffer) {
        buffer.put(check1.toByte())
        buffer.putShort(size.toShort())
        buffer.put(rawType.toByte())
        buffer.put(check2.toByte())
    }

    companion object {
        // how many bytes are used by the header
        const val HEADER_SIZE = 5

        @JvmStatic
        @Throws(ProtocolException::class)
        fun from(buffer: ByteBuffer): Header {
            val check1 = buffer.get().toUnsignedByte()
            val size = buffer.short
            val type = buffer.get()
            val check2 = buffer.get().toUnsignedByte()

            if (size.toInt() < HEADER_SIZE) throw ProtocolException("Invalid header size ${size}")

            val header = Header(type.toInt(), size.toInt())

            if (header.check1 != check1 || header.check2 != check2)
                throw ProtocolException("header checksum failed")

            return header
        }
    }
}