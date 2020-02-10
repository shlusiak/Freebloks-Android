package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameStateException
import org.junit.Assert.assertEquals
import org.junit.Test

class StoneTest {
    @Test
    fun test_default_available() {
        val stone = Stone(0)
        assertEquals(0, stone.available)
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

    @Test
    fun test_available_increment() {
        val s = Stone(2)
        s.availableIncrement()
        assertEquals(1, s.available)
    }
}