package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameStateException
import org.junit.Assert.assertEquals
import org.junit.Test

class StoneTest {
    @Test
    fun test_constructor() {
        val stone = Stone()

        assertEquals(0, stone.availableCount)
        assertEquals(0, stone.shape)
        assertEquals(1, stone.size)
    }

    @Test
    fun test_single_stone() {
        val stone = Stone()
        stone.init(0)

        assertEquals(0, stone.availableCount)
        assertEquals(0, stone.shape)
        assertEquals(1, stone.size)

        assertEquals(Mirrorable.Not, stone.mirrorable)
        assertEquals(Rotateable.Not, stone.rotateable)
        assertEquals(1, stone.points)

        assertEquals(1, stone.getStoneField(0, 0, 0, 0))
        assertEquals(1, stone.getStoneField(0, 0, 1, 0))
        assertEquals(1, stone.getStoneField(0, 0, 0, 1))
        assertEquals(8, stone.getStoneField(0, 1, 0, 0))
        assertEquals(8, stone.getStoneField(1, 0, 0, 0))
    }

    @Test
    fun test_stone_20() {
        val stone = Stone(19)
        assertEquals(5, stone.points)
    }

    @Test(expected = GameStateException::class)
    fun test_available_decrement_illegal() {
        val s = Stone()
        s.availableDecrement()
    }

    @Test
    fun test_available_decrement() {
        val s = Stone()
        s.setAvailable(1)
        assertEquals(1, s.availableCount)
        s.availableDecrement()
    }
}