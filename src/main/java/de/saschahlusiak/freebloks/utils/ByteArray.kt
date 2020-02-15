package de.saschahlusiak.freebloks.utils

fun ByteArray.toHexString(separator: String = ", "): String {
    return joinToString(separator) { String.format("0x%02x", it.toUnsignedByte()) }
}