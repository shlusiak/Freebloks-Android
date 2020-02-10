package de.saschahlusiak.freebloks.utils

import java.nio.ByteBuffer

fun byteBufferOf(vararg bytes: Int): ByteBuffer {
    val buffer = ByteBuffer.allocate(bytes.size)
    bytes.forEach { buffer.put(it.toByte()) }
    buffer.flip()
    return buffer
}