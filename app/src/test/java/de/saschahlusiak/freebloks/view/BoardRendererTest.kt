package de.saschahlusiak.freebloks.view

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.util.MockGL11
import org.junit.Before
import org.junit.Test
import javax.microedition.khronos.opengles.GL11

/**
 * Fairly meaningless tests, they just run on a mock object, but with nothing to assert.
 */
class BoardRendererTest {
    private lateinit var renderer: BoardRenderer
    private val gl: GL11 = MockGL11()

    @Before
    fun setup() {
        renderer = BoardRenderer()
    }

    @Test
    fun test_renderSingleStone() {
        renderer.renderSingleStone(gl, StoneColor.White, 1.0f)
        // no test
    }

    @Test
    fun test_renderShape() {
        renderer.renderShape(gl, StoneColor.White, Shape.get(5), Orientation.Default, 1.0f)
        // no test
    }
}