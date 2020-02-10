package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.utils.toUnsigned
import java.nio.ByteBuffer

/**
 * check1 uint8
 * data_length uint16
 * msg_type uint8
 * check2 uint8
 */
data class Header(val type: Int, val size: Int) {
    val check1: Int
        get() = ((size and 0x0055) xor type)

    val check2: Int
        get() = (check1 xor 0xD6) + type

    fun write(buffer: ByteBuffer) {
        buffer.put(check1.toByte())
        buffer.putShort(size.toShort())
        buffer.put(type.toByte())
        buffer.put(check2.toByte())
    }

    companion object {
        // how many bytes are used by the header
        const val HEADER_SIZE = 5

        @JvmStatic
        fun from(buffer: ByteBuffer): Header {
            val check1 = buffer.get().toUnsigned()
            val size = buffer.short
            val type = buffer.get()
            val check2 = buffer.get().toUnsigned()

            val header = Header(type.toInt(), size.toInt())

            if (header.check1 != check1 || header.check2 != check2)
                throw ProtocolException("header checksum failed")

            return header
        }
    }
}