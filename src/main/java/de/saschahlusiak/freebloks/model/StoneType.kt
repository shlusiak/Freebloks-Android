package de.saschahlusiak.freebloks.model

import java.io.Serializable

enum class Rotateable(val value: Int) {
    Not(1),
    Two(2),
    Four(4);
}

enum class Mirrorable {
    Not,
    Optional,
    Important
};

enum class Rotation {
    None,
    Right,
    Half,
    Left;

    fun rotateRight() = when(this) {
        None -> Right
        Right -> Half
        Half -> Left
        Left -> None
    }

    fun rotateLeft() = when(this) {
        None -> Left
        Left -> Half
        Half -> Right
        Right -> None
    }
}

class StoneType(
    val size: Int,
    val positionPoints: Int,
    val mirrorable: Mirrorable,
    val rotateable: Rotateable,
    val field: IntArray
): Serializable {
    val points: Int

    init {
        var p = 0
        for (x in 0 until size) for (y in 0 until size) {
            val value = getStoneField(x, y, false, Rotation.None)
            if (value == 1 || value == 2) {
                p++
            }
        }
        points = p
    }

    fun getStoneField(x: Int, y: Int, mirrored: Boolean, rotation: Rotation): Int {
        val ny: Int
        val nx: Int
        if (!mirrored) {
            when (rotation) {
                Rotation.None -> {
                    nx = x
                    ny = y
                }
                Rotation.Right -> {
                    nx = y
                    ny = size - 1 - x
                }
                Rotation.Half -> {
                    nx = size - 1 - x
                    ny = size - 1 - y
                }
                Rotation.Left -> {
                    nx = size - 1 - y
                    ny = x
                }
            }
        } else {
            when (rotation) {
                Rotation.None -> {
                    nx = x
                    ny = size - 1 - y
                }
                Rotation.Right -> {
                    nx = y
                    ny = x
                }
                Rotation.Half -> {
                    nx = size - 1 - x
                    ny = y
                }
                Rotation.Left -> {
                    nx = size - 1 - y
                    ny = size - 1 - x
                }
            }
        }

        return field[nx + ny * SIZE_MAX]
    }

    companion object {
        const val SIZE_MAX = 5
        const val COUNT = 21

        @JvmStatic
        fun get(shape: Int) = All[shape]

        @JvmField
        val All = arrayOf(
// 0
            StoneType(1, 8, Mirrorable.Not, Rotateable.Not, intArrayOf(
                1, 8, 8, 8, 8,    //0
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 1
            StoneType(2, 4, Mirrorable.Not, Rotateable.Two, intArrayOf(
                0, 1, 8, 8, 8,    //1
                0, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 2
            StoneType(2, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                1, 0, 8, 8, 8,    //2
                1, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 3
            StoneType(3, 4, Mirrorable.Not, Rotateable.Two, intArrayOf(
                0, 1, 0, 8, 8,    //3
                0, 2, 0, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 4
            StoneType(2, 8, Mirrorable.Not, Rotateable.Not, intArrayOf(
                1, 1, 8, 8, 8,    //4
                1, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 5
            StoneType(3, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //5
                0, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 6
            StoneType(3, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //6
                0, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 7
            StoneType(3, 2, Mirrorable.Important, Rotateable.Two, intArrayOf(
                0, 0, 0, 8, 8,    //7
                1, 1, 0, 8, 8,
                0, 1, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 8
            StoneType(4, 8, Mirrorable.Not, Rotateable.Two, intArrayOf(
                0, 0, 1, 0, 8,    //8
                0, 0, 2, 0, 8,
                0, 0, 2, 0, 8,
                0, 0, 1, 0, 8,
                8, 8, 8, 8, 8
            )),

// 9
            StoneType(3, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //9
                1, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 10
            StoneType(3, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                1, 1, 0, 8, 8,    //10
                0, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 11
            StoneType(3, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //11
                0, 2, 0, 8, 8,
                1, 2, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 12
            StoneType(3, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                1, 0, 0, 8, 8,    //12
                2, 0, 0, 8, 8,
                1, 2, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 13
            StoneType(3, 2, Mirrorable.Optional, Rotateable.Four, intArrayOf(
                1, 1, 0, 8, 8,    //13
                0, 1, 1, 8, 8,
                0, 0, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 14
            StoneType(3, 1, Mirrorable.Important, Rotateable.Two, intArrayOf(
                1, 0, 0, 8, 8,    //14
                1, 2, 1, 8, 8,
                0, 0, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 15
            StoneType(3, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                1, 0, 0, 8, 8,    //15
                1, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 16
            StoneType(3, 8, Mirrorable.Not, Rotateable.Not, intArrayOf(
                0, 1, 0, 8, 8,    //16
                1, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 17
            StoneType(4, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                0, 0, 1, 0, 8,    //17
                0, 0, 2, 0, 8,
                0, 0, 2, 0, 8,
                0, 1, 1, 0, 8,
                8, 8, 8, 8, 8
            )),

// 18
            StoneType(4, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                0, 0, 1, 0, 8,    //18
                0, 0, 2, 0, 8,
                0, 1, 1, 0, 8,
                0, 1, 0, 0, 8,
                8, 8, 8, 8, 8
            )),

// 19
            StoneType(4, 1, Mirrorable.Important, Rotateable.Four, intArrayOf(
                0, 1, 0, 0, 8,    //19
                0, 2, 1, 0, 8,
                0, 2, 0, 0, 8,
                0, 1, 0, 0, 8,
                8, 8, 8, 8, 8
            )),

// 20
            StoneType(5, 4, Mirrorable.Not, Rotateable.Two, intArrayOf(
                0, 0, 1, 0, 0,    //20
                0, 0, 2, 0, 0,
                0, 0, 2, 0, 0,
                0, 0, 2, 0, 0,
                0, 0, 1, 0, 0
            ))
        )
    }
}

