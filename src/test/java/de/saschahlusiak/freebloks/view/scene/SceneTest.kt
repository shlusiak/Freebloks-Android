package de.saschahlusiak.freebloks.view.scene

import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.utils.PointF
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SceneTest {
    private lateinit var scene: Scene

    private lateinit var game: Game

    @Before
    fun setup() {
        scene = Scene(null, null, null)
        game = scene.game
    }

    @Test
    fun testDefault() {
        assertEquals(0, scene.effects.size)
        assertEquals(3, scene.size)
        assertTrue(scene.hasAnimations())
        assertEquals(0, scene.basePlayer)
        assertEquals(0.0f, scene.baseAngle)
    }

    @Test
    fun testInvalidate() {
        assertFalse(scene.isInvalidated())
        scene.invalidate()
        // the call will reset the invalidated state
        assertTrue(scene.isInvalidated())
        assertFalse(scene.isInvalidated())
    }

    @Test
    fun test_rotateBoard_90() {
        val bo = scene.boardObject

        assertEquals(0, scene.basePlayer)
        assertEquals(0.0f, scene.baseAngle)
        assertEquals(0.0f, bo.currentAngle)
        assertFalse(bo.rotating)
        assertEquals(-1, bo.lastDetailsPlayer)

        assertTrue(scene.handlePointerDown(PointF(0f, 3f)))
        assertTrue(bo.rotating)

        assertTrue(scene.handlePointerMove(PointF(3f, 1f)))
        assertEquals(71.0f, bo.currentAngle, 1.0f)

        scene.handlePointerUp(PointF(3f, 0f))
        assertEquals(90.0f, bo.targetAngle)

        scene.execute(1.0f)
        assertEquals(90.0f, bo.currentAngle, 1.0f)

        assertEquals(1, bo.lastDetailsPlayer)

        bo.resetRotation()
        assertEquals(-1, bo.lastDetailsPlayer)
    }

    @Test
    fun test_rotateBoard_10() {
        val bo = scene.boardObject

        assertEquals(0.0f, bo.currentAngle)
        assertEquals(-1, bo.lastDetailsPlayer)

        assertTrue(scene.handlePointerDown(PointF(0f, 3f)))
        assertTrue(bo.rotating)

        assertTrue(scene.handlePointerMove(PointF(0.2f, 3f)))
        assertEquals(4.0f, bo.currentAngle, 1.0f)
        assertEquals(-1, bo.lastDetailsPlayer)

        assertTrue(scene.handlePointerMove(PointF(1f, 3f)))
        assertEquals(18.0f, bo.currentAngle, 1.0f)
        assertEquals(0, bo.lastDetailsPlayer)

        assertTrue(scene.handlePointerMove(PointF(0.2f, 3f)))
        assertEquals(4.0f, bo.currentAngle, 1.0f)
        assertEquals(-1, bo.lastDetailsPlayer)

        scene.handlePointerUp(PointF(0.2f, 3f))
        assertEquals(0.0f, bo.targetAngle, 1.0f)
    }
}