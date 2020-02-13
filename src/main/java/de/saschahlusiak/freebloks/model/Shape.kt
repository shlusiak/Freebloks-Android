package de.saschahlusiak.freebloks.model

import java.io.Serializable

/**
 * Immutable definition of a shape of a stone.
 *
 * See [Stone] for the member of [Player], which adds attributes like count and orientation.
 */
class Shape private constructor(
    // the number of the shape of this stone. must match with the number of this stone on the API.
    val number: Int,
    // the effective size of this stone, between 1 and 5
    val size: Int,
    val mirrorable: Mirrorable,
    val rotatable: Rotatable,

    /**
     * Field values.
     *
     * 0: free
     * 1: a corner of interest (can be placed on a "seed")
     * 2: a piece in the middle, used to optimise looking for possible moves
     * 8: invalid, out of bounds
     */
    val field: IntArray
) : Serializable {
    val points: Int

    val orientations: List<Orientation>

    init {
        var p = 0
        for (x in 0 until size) for (y in 0 until size) {
            val value = getStoneField(x, y, false, Rotation.None)
            if (value == 1 || value == 2) {
                p++
            }
        }
        points = p

        val mirrorMax: Int = if (mirrorable === Mirrorable.Important) 1 else 0
        var rotate: Int
        var mirror = 0

        orientations = sequence {
            while (mirror <= mirrorMax) {
                rotate = 0
                while (rotate < rotatable.value) {
                    yield(Orientation(mirror == 1, Rotation.from(rotate)))
                    rotate++
                }
                mirror++
            }
        }.toList()
    }

    /**
     * The shape number defines uniqueness
     */
    override fun hashCode() = number

    /**
     * The shape number defines uniqueness
     */
    override fun equals(other: Any?) = other is Shape && other.number == number

    /**
     * Returns the raw field value within the stone.
     *
     * @param x x coordinate within the stone, from 0..4
     * @param y y coordinate within the stone, from 0..4
     * @param mirrored whether the stone is mirrored over x (vertically)
     * @param rotation whether the stone is rotated
     */
    internal fun getStoneField(x: Int, y: Int, mirrored: Boolean, rotation: Rotation): Int {
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

    /**
     * Convenience function to return field value
     *
     * @see [getStoneField]
     */
    fun getStoneField(x: Int, y: Int, orientation: Orientation): Int {
        return getStoneField(x, y, orientation.mirrored, orientation.rotation)
    }

    /**
     * Whether given coordinate is free, i.e. not part of the stone
     */
    fun isFree(x: Int, y: Int, orientation: Orientation): Boolean {
        return isFree(x, y, orientation.mirrored, orientation.rotation)
    }

    /**
     * Whether given coordinate is free, i.e. not part of the stone
     */
    fun isFree(x: Int, y: Int, mirrored: Boolean, rotation: Rotation): Boolean {
        return getStoneField(x, y, mirrored, rotation) == 0
    }

    /**
     * Whether the given coordinate is occupied by the stone
     */
    fun isStone(x: Int, y: Int, mirrored: Boolean, rotation: Rotation): Boolean {
        return getStoneField(x, y, mirrored, rotation) != 0
    }

    fun isStone(x: Int, y: Int, orientation: Orientation): Boolean {
        return isStone(x, y, orientation.mirrored, orientation.rotation)
    }

    /**
     * Whether the given coordinate is a corner of interest, i.e. whether it makes sense to
     * test this stone on this piece against a "seed" on the board.
     */
    fun isCorner(x: Int, y: Int, orientation: Orientation): Boolean {
        return isCorner(x, y, orientation.mirrored, orientation.rotation)
    }

    /**
     * Whether the given coordinate is a corner of interest, i.e. whether it makes sense to
     * test this stone on this piece against a "seed" on the board.
     */
    fun isCorner(x: Int, y: Int, mirrored: Boolean, rotation: Rotation): Boolean {
        return getStoneField(x, y, mirrored, rotation) == 1
    }

    companion object {
        const val SIZE_MAX = 5
        const val COUNT = 21

        @JvmStatic
        fun get(shape: Int) = All[shape]

        val All = arrayOf(
// 0
            Shape(0, 1, Mirrorable.Not, Rotatable.Not, intArrayOf(
                1, 8, 8, 8, 8,    //0
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 1
            Shape(1, 2, Mirrorable.Not, Rotatable.Two, intArrayOf(
                0, 1, 8, 8, 8,    //1
                0, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 2
            Shape(2, 2, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                1, 0, 8, 8, 8,    //2
                1, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 3
            Shape(3, 3, Mirrorable.Not, Rotatable.Two, intArrayOf(
                0, 1, 0, 8, 8,    //3
                0, 2, 0, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 4
            Shape(4, 2, Mirrorable.Not, Rotatable.Not, intArrayOf(
                1, 1, 8, 8, 8,    //4
                1, 1, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 5
            Shape(5, 3, Mirrorable.Important, Rotatable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //5
                0, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 6
            Shape(6, 3, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //6
                0, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 7
            Shape(7, 3, Mirrorable.Important, Rotatable.Two, intArrayOf(
                0, 0, 0, 8, 8,    //7
                1, 1, 0, 8, 8,
                0, 1, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 8
            Shape(8, 4, Mirrorable.Not, Rotatable.Two, intArrayOf(
                0, 0, 1, 0, 8,    //8
                0, 0, 2, 0, 8,
                0, 0, 2, 0, 8,
                0, 0, 1, 0, 8,
                8, 8, 8, 8, 8
            )),

// 9
            Shape(9, 3, Mirrorable.Important, Rotatable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //9
                1, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 10
            Shape(10, 3, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                1, 1, 0, 8, 8,    //10
                0, 2, 0, 8, 8,
                1, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 11
            Shape(11, 3, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                0, 1, 0, 8, 8,    //11
                0, 2, 0, 8, 8,
                1, 2, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 12
            Shape(12, 3, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                1, 0, 0, 8, 8,    //12
                2, 0, 0, 8, 8,
                1, 2, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 13
            Shape(13, 3, Mirrorable.Optional, Rotatable.Four, intArrayOf(
                1, 1, 0, 8, 8,    //13
                0, 1, 1, 8, 8,
                0, 0, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 14
            Shape(14, 3, Mirrorable.Important, Rotatable.Two, intArrayOf(
                1, 0, 0, 8, 8,    //14
                1, 2, 1, 8, 8,
                0, 0, 1, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 15
            Shape(15, 3, Mirrorable.Important, Rotatable.Four, intArrayOf(
                1, 0, 0, 8, 8,    //15
                1, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 16
            Shape(16, 3, Mirrorable.Not, Rotatable.Not, intArrayOf(
                0, 1, 0, 8, 8,    //16
                1, 2, 1, 8, 8,
                0, 1, 0, 8, 8,
                8, 8, 8, 8, 8,
                8, 8, 8, 8, 8
            )),

// 17
            Shape(17, 4, Mirrorable.Important, Rotatable.Four, intArrayOf(
                0, 0, 1, 0, 8,    //17
                0, 0, 2, 0, 8,
                0, 0, 2, 0, 8,
                0, 1, 1, 0, 8,
                8, 8, 8, 8, 8
            )),

// 18
            Shape(18, 4, Mirrorable.Important, Rotatable.Four, intArrayOf(
                0, 0, 1, 0, 8,    //18
                0, 0, 2, 0, 8,
                0, 1, 1, 0, 8,
                0, 1, 0, 0, 8,
                8, 8, 8, 8, 8
            )),

// 19
            Shape(19, 4, Mirrorable.Important, Rotatable.Four, intArrayOf(
                0, 1, 0, 0, 8,    //19
                0, 2, 1, 0, 8,
                0, 2, 0, 0, 8,
                0, 1, 0, 0, 8,
                8, 8, 8, 8, 8
            )),

// 20
            Shape(20, 5, Mirrorable.Not, Rotatable.Two, intArrayOf(
                0, 0, 1, 0, 0,    //20
                0, 0, 2, 0, 0,
                0, 0, 2, 0, 0,
                0, 0, 2, 0, 0,
                0, 0, 1, 0, 0
            ))
        )
    }
}

