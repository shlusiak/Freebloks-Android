package de.saschahlusiak.freebloks.utils

fun ByteArray.hexString(separator: String = ", "): String {
    return joinToString(separator) { String.format("0x%02x", it.toUnsignedByte()) }
}