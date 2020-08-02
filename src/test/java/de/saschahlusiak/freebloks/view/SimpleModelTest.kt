package de.saschahlusiak.freebloks.view

import org.junit.Assert.*
import org.junit.Test

class SimpleModelTest {
    @Test
    fun test_simpleModel() {
        val m = SimpleModel(4, 2).apply {
            addVertex(0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f)
            addVertex(1f, 0f, 0f, 0f, 1f, 0f, 1f, 0f)
            addVertex(1f, 1f, 0f, 0f, 1f, 0f, 1f, 1f)
            addVertex(0f,1f, 0f, 0f, 1f, 0f, 0f, 1f)

            addIndex(0, 1, 2)
            addIndex(0, 3, 2)

            assertEquals(32, vertexBuffer.position())
            assertEquals(6, indexBuffer.position())
            assertNull(vbo)

            commit()
        }

        assertNotNull(m)
    }

}
