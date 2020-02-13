package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageType
import java.nio.ByteBuffer

data class MessageRequestGameMode(
    val width: Int,
    val height: Int,
    val gameMode: GameMode,
    val stoneNumbers: IntArray
) : Message(MessageType.RequestGameMode, 25) {
    init {
        assert(stoneNumbers.size == Shape.COUNT) { "invalid stoneNumbers size of ${stoneNumbers.size}" }
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(minVersion)
        buffer.put(width.toByte())
        buffer.put(height.toByte())
        buffer.put(gameMode.ordinal.toByte())
        stoneNumbers.forEach { buffer.put(it.toByte()) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageRequestGameMode

        if (width != other.width) return false
        if (height != other.height) return false
        if (gameMode != other.gameMode) return false
        if (!stoneNumbers.contentEquals(other.stoneNumbers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + gameMode.hashCode()
        result = 31 * result + stoneNumbers.contentHashCode()
        return result
    }

    companion object {
        private const val minVersion: Byte = 1

        fun from(data: ByteBuffer): MessageRequestGameMode {
            val version = data.get().toInt()
            assert(version >= minVersion) { "Unknown version $version" }
            val width = data.get().toInt()
            val height = data.get().toInt()
            val gameMode = GameMode.from(data.get().toInt())
            val stoneNumbers = IntArray(Shape.COUNT) { data.get().toInt() }
            return MessageRequestGameMode(width, height, gameMode, stoneNumbers)
        }
    }
}