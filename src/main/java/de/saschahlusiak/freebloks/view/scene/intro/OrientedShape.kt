package de.saschahlusiak.freebloks.view.scene.intro

import de.saschahlusiak.freebloks.model.Mirrorable
import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Rotation
import de.saschahlusiak.freebloks.model.Shape
import java.io.Serializable

/**
 * A stone that has a [Shape] and an [Orientation], used in [Intro]
 */
internal data class OrientedShape(
    val shape: Shape,
    var orientation: Orientation = Orientation()
): Serializable {
    @JvmOverloads
    constructor(shape: Int, mirrored: Boolean = false, rotation: Rotation = Rotation.None): this(Shape.get(shape), Orientation(mirrored, rotation))

    fun rotateLeft() {
        orientation = orientation.rotatedLeft(shape.rotatable)
    }

    fun rotateRight() {
        orientation = orientation.rotatedRight(shape.rotatable)
    }

    fun rotate180Degrees() {
        orientation = orientation.rotatedLeft(shape.rotatable).rotatedLeft(shape.rotatable)
    }

    fun mirrorVertically() {
        if (shape.mirrorable === Mirrorable.Not) return
        orientation = orientation.mirroredVertically()
    }

    fun mirrorHorizontally() {
        if (shape.mirrorable === Mirrorable.Not) return
        orientation = orientation.mirroredHorizontally()
    }

    fun getStoneField(x: Int, y: Int) = shape.getStoneField(x, y, orientation)
    fun isStone(x: Int, y: Int) = shape.isStone(x, y, orientation)
    fun isFree(x: Int, y: Int) = shape.isFree(x, y, orientation)
    fun isCorner(x: Int, y: Int) = shape.isCorner(x, y, orientation)
}