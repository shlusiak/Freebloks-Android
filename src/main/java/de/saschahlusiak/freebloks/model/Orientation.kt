package de.saschahlusiak.freebloks.model

import java.io.Serializable

/**
 * An immutable orientation of a stone, with mirroring over x-axis (vertical) and rotation
 *
 * @param mirrored mirrored vertically, over x-axis
 * @param rotation current rotation
 */
data class Orientation(val mirrored: Boolean = false, val rotation: Rotation = Rotation.None): Serializable {

    fun rotatedLeft(rotatable: Rotatable = Rotatable.Four) = Orientation(mirrored, rotation.rotateLeft(rotatable))

    fun rotatedRight(rotatable: Rotatable = Rotatable.Four) = Orientation(mirrored, rotation.rotateRight(rotatable))

    fun mirroredVertically() = when (rotation) {
        Rotation.Right -> Orientation(!mirrored, Rotation.Left)
        Rotation.Left -> Orientation(!mirrored, Rotation.Right)
        else -> Orientation(!mirrored, rotation)
    }

    fun mirroredHorizontally() = when (rotation) {
        Rotation.None -> Orientation(!mirrored, Rotation.Half)
        Rotation.Half -> Orientation(!mirrored, Rotation.None)
        else -> Orientation(!mirrored, rotation)
    }
}