package de.saschahlusiak.freebloks.model

import java.io.Serializable

class Turn(val player: Int, val shape: Shape, val orientation: Orientation, val y: Int, val x: Int) : Serializable {

    constructor(from: Turn): this(from.player, from.shape, from.orientation, from.y, from.x)

    @Deprecated(message = "Use Orientation instead")
    constructor(player: Int, shape: Int, y: Int, x: Int, mirror: Int, rotate: Int): this(player, Shape.get(shape), Orientation(mirror == 1, Rotation.from(rotate)), y, x)

    constructor(player: Int, shape: Int, y: Int, x: Int, orientation: Orientation) : this(player, Shape.get(shape), orientation, y, x)

    val shapeNumber = shape.number

    @Deprecated("Should not be needed anymore")
    val mirrorCount = if (orientation.mirrored) 1 else 0

    @Deprecated("Should not be needed anymore")
    val rotationCount = orientation.rotation.value

    companion object {
        private const val serialVersionUID = -1715006791524885742L
    }
}