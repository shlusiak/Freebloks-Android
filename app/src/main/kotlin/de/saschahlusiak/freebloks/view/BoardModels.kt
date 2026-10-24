package de.saschahlusiak.freebloks.view

/**
 * The geometry of the board, stones and shadows, shared by the GLES 1.x and GLES 2.0 renderers.
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
     * Builds a SimpleModel for a single field of the board, which is half a stone and is "deep".
     */
    fun buildSingleBoardFieldModel(): SimpleModel {
        return SimpleModel(8, 10, true).apply {
            /* bottom, inner */
            addVertex(-r2, y1, +r2, 0f, 1f, 0f, -r2, r2)
            addVertex(+r2, y1, +r2, 0f, 1f, 0f, r2, r2)
            addVertex(+r2, y1, -r2, 0f, 1f, 0f, r2, -r2)
            addVertex(-r2, y1, -r2, 0f, 1f, 0f, -r2, -r2)

            /* top, outer */
            addVertex(-r1, y2, +r1, +1f, 1f, -1f, -r1, r1)
            addVertex(+r1, y2, +r1, -1f, 1f, -1f, r1, r1)
            addVertex(+r1, y2, -r1, -1f, 1f, +1f, r1, -r1)
            addVertex(-r1, y2, -r1, +1f, 1f, +1f, -r1, -r1)

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

            commit()
        }
    }

    /**
     * Builds the planar shadow quad for a single stone
     */
    fun buildStoneShadow(): SimpleModel {
        return SimpleModel(4, 2, false).apply {
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
        return SimpleModel(12, 20, true).apply {
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
        return SimpleModel(12, 6, true).apply {
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
