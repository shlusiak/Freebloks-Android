package de.saschahlusiak.freebloks.model

import java.io.Serializable

data class Turn(val player: Int, val shape: Shape, val orientation: Orientation, val y: Int, val x: Int) : Serializable {

    constructor(from: Turn): this(from.player, from.shape, from.orientation, from.y, from.x)

    constructor(player: Int, shape: Int, y: Int, x: Int, orientation: Orientation) : this(player, Shape.get(shape), orientation, y, x)

    val shapeNumber = shape.number

    companion object {
        private const val serialVersionUID = -1715006791524885742L
    }
}