package de.saschahlusiak.freebloks.utils

import java.nio.ByteBuffer

fun byteBufferOf(vararg bytes: Int): ByteBuffer {
    val buffer = ByteBuffer.allocate(bytes.size)
    bytes.forEach { buffer.put(it.toByte()) }
    buffer.flip()
    return buffer
}

/**
 * Writes a portion of bytes into the buffer, with optional padding.
 *
 * Keep in mind to leave space for a trailing 0, in case bytes exceed maxLength, as the entire maxLength will be written.
 *
 * @param bytes bytes to write
 * @param maxLength maximum length to write, excess bytes will be capped, missing bytes will be filled as 0
 */
fun ByteBuffer.put(bytes: ByteArray, maxLength: Int): ByteBuffer {
    if (bytes.size <= maxLength) {
        put(bytes)
        repeat(maxLength - bytes.size) { put(0) }
    } else {
        for (i in 0 until maxLength) put(bytes[i])
    }
    return this
}

fun ByteBuffer.getArray(count: Int) = ByteArray(count) { get() }

fun ByteBuffer.forRemaining(block: (Byte) -> (Unit)) {
    while (hasRemaining()) {
        block.invoke(get())
    }
}