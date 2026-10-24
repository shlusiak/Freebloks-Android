package de.saschahlusiak.freebloks.view

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class BoardModelsTest {
    private val s = BoardRenderer.stoneSize

    @Test
    fun test_boardModel_sizes() {
        val field = BoardModels.buildSingleBoardFieldModel()
        val board = BoardModels.buildBoardModel(3, 2, s * 4f, 0.12f, 1f)

        assertEquals(8 * 8, field.vertexBuffer.capacity())
        assertEquals(6 * 8 * 8, board.vertexBuffer.capacity())
        assertEquals(30, field.numIndices)
        assertEquals(6 * 30, board.numIndices)

        // all indices point into the vertices
        for (i in 0 until board.numIndices) {
            assertTrue(board.indexBuffer.get(i) in 0 until 6 * 8)
        }
    }

    @Test
    fun test_boardModel_matches_single_field() {
        val field = BoardModels.buildSingleBoardFieldModel()
        // identity texture transform, so the texture coordinates are the plain position in the plane
        val board = BoardModels.buildBoardModel(1, 1, s * 4f, 1f, 0f)

        for (i in 0 until field.vertexBuffer.capacity()) {
            assertEquals(field.vertexBuffer.get(i), board.vertexBuffer.get(i), 1e-6f)
        }
    }

    @Test
    fun test_boardModel_bakes_texture_transform() {
        val rotation = 1f
        val scale = 0.12f
        val board = BoardModels.buildBoardModel(3, 2, s * 4f, scale, rotation)

        // field x = 2, y = 1, first vertex
        val field = BoardModels.buildSingleBoardFieldModel()
        val base = (1 * 3 + 2) * 8 * 8
        val px = field.vertexBuffer.get(0)
        val pz = field.vertexBuffer.get(2)

        // position is shifted to the field, centered around the origin
        assertEquals(-s * 2 + s * 2 * 2 + px, board.vertexBuffer.get(base), 1e-5f)
        assertEquals(-s * 1 + s * 2 * 1 + pz, board.vertexBuffer.get(base + 2), 1e-5f)

        // texture coordinate: shift by the field, then rotate, then scale
        val u = px + s * 4f * 2
        val t = pz + s * 4f * 1
        val a = Math.toRadians(rotation.toDouble())
        assertEquals((u * cos(a) - t * sin(a)).toFloat() * scale, board.vertexBuffer.get(base + 6), 1e-5f)
        assertEquals((u * sin(a) + t * cos(a)).toFloat() * scale, board.vertexBuffer.get(base + 7), 1e-5f)
    }
}
