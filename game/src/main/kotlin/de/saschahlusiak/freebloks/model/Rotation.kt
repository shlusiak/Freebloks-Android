package de.saschahlusiak.freebloks.model

enum class Rotation(val value: Int) {
    None(0),
    Right(1),
    Half(2),
    Left(3);

    fun rotateRight(rotatable: Rotatable) = when (rotatable) {
        Rotatable.Not -> this
        Rotatable.Two -> when (this) {
            None -> Right
            else -> None
        }
        Rotatable.Four -> when (this) {
            None -> Right
            Right -> Half
            Half -> Left
            Left -> None
        }
    }

    fun rotateLeft(rotatable: Rotatable) = when (rotatable) {
        Rotatable.Not -> this
        Rotatable.Two -> when (this) {
            None -> Right
            else -> None
        }
        Rotatable.Four -> when (this) {
            None -> Left
            Left -> Half
            Half -> Right
            Right -> None
        }
    }

    companion object {
        fun from(rotation: Int) = entries[rotation]
    }
}