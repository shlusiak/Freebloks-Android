package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameStateException
import org.junit.Assert.assertEquals
import org.junit.Test

class StoneTest {
    @Test
    fun test_single_stone() {
        val stone = Stone(0)

        assertEquals(0, stone.available)
        assertEquals(0, stone.shape.number)
        assertEquals(1, stone.shape.size)

        assertEquals(Mirrorable.Not, stone.shape.mirrorable)
        assertEquals(Rotatable.Not, stone.shape.rotatable)
        assertEquals(1, stone.shape.points)

        assertEquals(1, stone.shape.getStoneField(0, 0, false, Rotation.None))
        assertEquals(1, stone.shape.getStoneField(0, 0, true, Rotation.None))
        assertEquals(1, stone.shape.getStoneField(0, 0, false, Rotation.Right))
        assertEquals(8, stone.shape.getStoneField(0, 1, false, Rotation.None))
        assertEquals(8, stone.shape.getStoneField(1, 0, false, Rotation.None))
    }

    @Test
    fun test_stone_20() {
        val stone = Stone(19)
        assertEquals(5, stone.shape.points)
    }

    @Test(expected = GameStateException::class)
    fun test_available_decrement_illegal() {
        val s = Stone(2)
        s.availableDecrement()
    }

    @Test
    fun test_available_decrement() {
        val s = Stone(3)
        s.available = 1
        assertEquals(1, s.available)
        s.availableDecrement()
    }
}