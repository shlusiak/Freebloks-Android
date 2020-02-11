package de.saschahlusiak.freebloks.utils

import java.io.InputStream
import java.nio.ByteBuffer

fun InputStream.read(buffer: ByteBuffer, length: Int): Int {
    return read(buffer.array(), buffer.position(), length).also {
        if (it > 0) buffer.position(buffer.position() + it)
    }
}