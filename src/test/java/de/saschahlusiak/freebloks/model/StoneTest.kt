package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameStateException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.IllegalStateException

class StoneTest {
    @Test
    fun test_constructor() {
        val stone = Stone()

        assertEquals(0, stone._available)
        assertEquals(0, stone._stone_shape)
        assertEquals(0, stone._stone_size)
    }

    @Test
    fun test_single_stone() {
        val stone = Stone()
        stone.init(0)

        assertEquals(0, stone._available)
        assertEquals(0, stone._stone_shape)
        assertEquals(1, stone._stone_size)

        assertEquals(Stone.MIRRORABLE_NOT, stone._mirrorable)
        assertEquals(Stone.ROTATEABLE_NOT, stone._rotateable)
        assertEquals(1, stone._stone_points)

        assertEquals(1, stone.get_stone_field(0, 0, 0, 0))
        assertEquals(1, stone.get_stone_field(0, 0, 1, 0))
        assertEquals(1, stone.get_stone_field(0, 0, 0, 1))
        assertEquals(8, stone.get_stone_field(0, 1, 0, 0))
        assertEquals(8, stone.get_stone_field(1, 0, 0, 0))
    }

    @Test(expected = GameStateException::class)
    fun test_available_decrement_illegal() {
        val s = Stone()
        s.available_decrement()
    }

    @Test
    fun test_available_decrement() {
        val s = Stone()
        s._available = 1
        assertEquals(1, s._available)
        s.available_decrement()
    }
}