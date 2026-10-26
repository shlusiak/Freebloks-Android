package de.saschahlusiak.freebloks.view.gles2

import org.junit.Assert.assertEquals
import org.junit.Test

class Gles2ModelTest {
    @Test
    fun test_model() {
        val m = Gles2Model(4, 2).apply {
            addVertex(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f)
            addVertex(1f, 0f, 0f, 0f, 1f, 0f, 1f, 0f)
            addVertex(1f, 1f, 0f, 0f, 1f, 0f, 1f, 1f)
            addVertex(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f)

            addIndex(0, 1, 2)
            addIndex(0, 3, 2)

            assertEquals(32, vertexBuffer.position())
            assertEquals(6, indexBuffer.position())

            commit()
        }

        assertEquals(6, m.numIndices)
        assertEquals(0, m.vertexBuffer.position())
        assertEquals(0, m.indexBuffer.position())
    }

    @Test
    fun test_index_offset() {
        val m = Gles2Model(8, 2).apply {
            addIndex(4, 0, 1, 2)
            addIndex(4, 0, 2, 3)
            commit()
        }

        val expected = shortArrayOf(4, 5, 6, 4, 6, 7)
        for (i in expected.indices) assertEquals(expected[i], m.indexBuffer.get(i))
    }
}
