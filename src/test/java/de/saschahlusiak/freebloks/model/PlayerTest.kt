package de.saschahlusiak.freebloks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {
    @Test
    fun test_constructor() {
        val p = Player(2)

        assertEquals(2, p.number)
        assertEquals(Shape.COUNT, p.stones.size)
        p.stones.forEach {
            assertEquals(0, it.available)

        }
    }
}
