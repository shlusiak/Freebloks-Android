package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.nio.ByteBuffer

data class MessageSetStone(
    val player: Int,
    val shape: Int,
    val mirrored: Boolean,
    val rotation: Rotation,
    val x: Int,
    val y: Int
): Message(MessageType.SetStone, 6) {
    init {
        assert(player in 0..3) { "Player $player must be between 0 and 3"}
        assert(shape in 0..Shape.COUNT) { "Invalid shape $shape" }
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(player.toByte())
        buffer.put(shape.toByte())
        buffer.put((if (mirrored) 1 else 0).toByte())
        buffer.put(rotation.value.toByte())
        buffer.put(x.toByte())
        buffer.put(y.toByte())
    }

    companion object {
        fun from(data: ByteBuffer): MessageSetStone {
            val player = data.get().toInt()
            val shape = data.get().toUnsignedByte()
            val mirrored = data.get().toInt() == 1
            val rotation = Rotation.from(data.get().toInt())
            val x = data.get().toInt()
            val y = data.get().toInt()

            return MessageSetStone(player, shape, mirrored, rotation, x, y)
        }
    }
}