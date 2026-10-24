package de.saschahlusiak.freebloks.view

/**
 * The geometry of the board, stones and shadows, used by the renderer.
 */
object BoardModels {
    private const val stoneSize = BoardRenderer.stoneSize

    private const val bevelSize = 0.18f
    private const val bevelHeight = 0.25f
    private const val borderBottom = -1.1f
    private const val borderTop = 0.4f
    private const val borderWidth = 0.5f

    private const val r1 = stoneSize
    private const val r2 = stoneSize - bevelSize
    private const val y1 = -bevelHeight
    private const val y2 = 0.0f

    /**
     * The vertices of a single field as x, y, z, normal x, y, z. The texture coordinate is the position in the
     * plane (x, z), so the texture runs continuously over neighbouring fields.
     */
    private val fieldVertices = arrayOf(
        /* bottom, inner */
        floatArrayOf(-r2, y1, +r2, 0f, 1f, 0f),
        floatArrayOf(+r2, y1, +r2, 0f, 1f, 0f),
        floatArrayOf(+r2, y1, -r2, 0f, 1f, 0f),
        floatArrayOf(-r2, y1, -r2, 0f, 1f, 0f),

        /* top, outer */
        floatArrayOf(-r1, y2, +r1, +1f, 1f, -1f),
        floatArrayOf(+r1, y2, +r1, -1f, 1f, -1f),
        floatArrayOf(+r1, y2, -r1, -1f, 1f, +1f),
        floatArrayOf(-r1, y2, -r1, +1f, 1f, +1f)
    )

    private val fieldIndices = intArrayOf(
        0, 1, 2,
        0, 2, 3,
        0, 5, 1,
        0, 4, 5,
        1, 5, 6,
        1, 6, 2,
        2, 6, 7,
        2, 7, 3,
        3, 7, 4,
        3, 4, 0
    )

    /**
     * Builds a SimpleModel for a single field of the board, which is half a stone and is "deep".
     */
    fun buildSingleBoardFieldModel(): SimpleModel {
        return SimpleModel(fieldVertices.size, fieldIndices.size / 3).apply {
            for (v in fieldVertices) addVertex(v[0], v[1], v[2], v[3], v[4], v[5], v[0], v[2])
            for (i in fieldIndices.indices step 3) addIndex(fieldIndices[i], fieldIndices[i + 1], fieldIndices[i + 2])
            commit()
        }
    }

    /**
     * Builds one model of all fields of a [width] x [height] board, centered around the origin.
     *
     * The texture coordinates are baked: the texture is shifted by [textureShift] per field, so it continues
     * across fields, then rotated by [textureRotation] degrees and scaled by [textureScale].
     */
    fun buildBoardModel(width: Int, height: Int, textureShift: Float, textureScale: Float, textureRotation: Float): SimpleModel {
        val perField = fieldVertices.size
        require(width * height * perField <= 0xffff) { "board of $width x $height is too big for 16 bit indices" }

        val angle = Math.toRadians(textureRotation.toDouble())
        val cos = kotlin.math.cos(angle).toFloat()
        val sin = kotlin.math.sin(angle).toFloat()

        return SimpleModel(width * height * perField, width * height * fieldIndices.size / 3).apply {
            for (y in 0 until height) for (x in 0 until width) {
                val cx = -stoneSize * (width - 1) + stoneSize * 2.0f * x
                val cz = -stoneSize * (height - 1) + stoneSize * 2.0f * y

                for (v in fieldVertices) {
                    val u = v[0] + textureShift * x
                    val t = v[2] + textureShift * y
                    addVertex(
                        cx + v[0], v[1], cz + v[2], v[3], v[4], v[5],
                        (u * cos - t * sin) * textureScale,
                        (u * sin + t * cos) * textureScale
                    )
                }
                val base = (y * width + x) * perField
                for (i in fieldIndices.indices step 3) {
                    addIndex(base, fieldIndices[i], fieldIndices[i + 1], fieldIndices[i + 2])
                }
            }
            commit()
        }
    }

    /**
     * Builds the planar shadow quad for a single stone
     */
    fun buildStoneShadow(): SimpleModel {
        return SimpleModel(4, 2).apply {
            addVertex(-r1, 0f, +r1, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r1, 0f, +r1, 0f, 1f, 0f, 1f, 0f)
            addVertex(+r1, 0f, -r1, 0f, 1f, 0f, 1f, 1f)
            addVertex(-r1, 0f, -r1, 0f, 1f, 0f, 0f, 1f)
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            commit()
        }
    }

    /**
     * Builds the model of a full player's stone, including top and bottom
     */
    fun buildStoneModel(): SimpleModel {
        return SimpleModel(12, 20).apply {
            /* top, inner */
            addVertex(-r2, -y1, +r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r2, -y1, +r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(+r2, -y1, -r2, 0f, 1f, 0f, 0f, 0f)
            addVertex(-r2, -y1, -r2, 0f, 1f, 0f, 0f, 0f)

            /* middle, outer */
            addVertex(-r1, y2, +r1, -1f, 0f, +1f, 0f, 0f)
            addVertex(+r1, y2, +r1, +1f, 0f, +1f, 0f, 0f)
            addVertex(+r1, y2, -r1, +1f, 0f, -1f, 0f, 0f)
            addVertex(-r1, y2, -r1, -1f, 0f, -1f, 0f, 0f)

            /* bottom, inner */
            addVertex(-r2, y1, +r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(+r2, y1, +r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(+r2, y1, -r2, 0f, -1f, 0f, 0f, 0f)
            addVertex(-r2, y1, -r2, 0f, -1f, 0f, 0f, 0f)

            /* top */
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            addIndex(0, 5, 1)
            addIndex(0, 4, 5)
            addIndex(1, 5, 6)
            addIndex(1, 6, 2)
            addIndex(2, 6, 7)
            addIndex(2, 7, 3)
            addIndex(3, 7, 4)
            addIndex(3, 4, 0)

            /* bottom */
            addIndex(8, 10, 9)
            addIndex(8, 11, 10)
            addIndex(8, 9, 5)
            addIndex(8, 5, 4)
            addIndex(9, 6, 5)
            addIndex(9, 10, 6)
            addIndex(10, 7, 6)
            addIndex(10, 11, 7)
            addIndex(11, 4, 7)
            addIndex(11, 8, 4)
            commit()
        }
    }

    /**
     * Builds the square model of the border of the field with the given size
     */
    fun buildBorderModel(boardSize: Int): SimpleModel {
        return SimpleModel(12, 6).apply {
            val w1 = stoneSize * boardSize
            val w2 = w1 + borderWidth

            /* front */addVertex(w2, borderTop, w2, 0f, 0f, 1f, w2, borderTop)
            addVertex(-w2, borderTop, w2, 0f, 0f, 1f, -w2, borderTop)
            addVertex(-w2, borderBottom, w2, 0f, 0f, 1f, -w2, borderBottom)
            addVertex(w2, borderBottom, w2, 0f, 0f, 1f, w2, borderBottom)

            /* top */addVertex(w2, borderTop, w2, 0f, 1f, 0f, w2, w2)
            addVertex(-w2, borderTop, w2, 0f, 1f, 0f, -w2, w2)
            addVertex(-w1, borderTop, w1, 0f, 1f, 0f, -w1, w1)
            addVertex(w1, borderTop, w1, 0f, 1f, 0f, w1, w1)

            /* inner */addVertex(w1, borderTop, w1, 0f, 0f, -1f, w1, borderTop)
            addVertex(-w1, borderTop, w1, 0f, 0f, -1f, -w1, borderTop)
            addVertex(-w1, 0f, w1, 0f, 0f, -1f, -w1, w1)
            addVertex(w1, 0f, w1, 0f, 0f, -1f, w1, w1)
            addIndex(0, 1, 2)
            addIndex(0, 2, 3)
            addIndex(7, 6, 4)
            addIndex(4, 6, 5)
            addIndex(9, 8, 10)
            addIndex(8, 11, 10)

            commit()
        }
    }
}
