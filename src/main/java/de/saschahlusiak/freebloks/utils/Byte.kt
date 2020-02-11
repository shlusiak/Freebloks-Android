package de.saschahlusiak.freebloks.utils

/**
 * Bytes on the JVM are always signed 8 bit, ranging from -128..127
 * Converting them to an Int would result in 0xFF = (Byte) -1 = (Int) -1 = 0xFFFFFFFF.
 *
 * This function converts a Byte into an int ranging from 0..255.
 */
fun Byte.toUnsigned(): Int = this.toInt() and 0xFF

fun ubyteArrayOf(vararg bytes: Int): ByteArray {
    return ByteArray(bytes.size) { index -> (bytes[index] and 0xFF).toByte() }
}