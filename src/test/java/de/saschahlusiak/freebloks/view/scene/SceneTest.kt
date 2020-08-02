package de.saschahlusiak.freebloks.view.scene

import de.saschahlusiak.freebloks.model.*
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

        game.board.setAvailableStones(GameConfig.DEFAULT_STONE_SET)
        game.board.startNewGame(GameMode.DEFAULT)
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

    @Test
    fun test_commitStone() {
        val wheel = scene.wheel
        val stone = scene.currentStone
        val board = game.board

        scene.snapAid = true

        game.currentPlayer = 0
        game.setPlayerType(0, Game.PLAYER_LOCAL)

        assertEquals(-1, wheel.currentPlayer)
        wheel.update(0)
        assertEquals(21, wheel.stones.size)
        assertEquals(0, wheel.currentPlayer)

        assertEquals(Wheel.Status.IDLE, wheel.status)
        // Grab the stone from here. The wheel is not moved, so it is all the way to the left
        scene.handlePointerDown(PointF(0.0f, 18.0f))
        assertEquals(Wheel.Status.SPINNING, wheel.status)
        assertEquals(CurrentStone.Status.IDLE, stone.status)
        assertEquals(1, wheel.currentStone?.shape?.number)

        // Move to the center of the board, which will take it out of the wheel into currentStone
        scene.handlePointerMove(PointF(0.0f, 0.0f))
        assertEquals(Wheel.Status.IDLE, wheel.status)
        assertEquals(1, stone.stone?.shape?.number)
        assertEquals(CurrentStone.Status.DRAGGING, stone.status)
        assertEquals(9.0f, stone.pos.x, 1.0f)
        assertEquals(9.0f, stone.pos.y, 1.0f)
        assertFalse(stone.isValid)
        assertEquals(1, wheel.currentStone?.shape?.number)

        scene.handlePointerMove(PointF(-7.9f, 8.5f))

        val turn = stone.asTurn()!!
        assertEquals(Turn(0, 1, 18, -1, Orientation.Default), turn)
        assertTrue(stone.isValid)
        assertTrue(board.isValidTurn(turn))

        // simulate a tap gesture
        scene.handlePointerUp(PointF(-7.9f, 8.5f))
        scene.handlePointerDown(PointF(-7.9f, 8.5f))
        scene.handlePointerUp(PointF(-7.9f, 8.5f))

        assertEquals(CurrentStone.Status.IDLE, stone.status)
        assertNull(stone.stone)
        assertNull(wheel.currentStone)
        assertFalse(stone.isValid)

        // the stone wasn't actually set but only requested to be
        assertTrue(board.isValidTurn(turn))
    }
}