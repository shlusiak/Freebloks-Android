package de.saschahlusiak.freebloks.model

import org.junit.Assert.*
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
        assertEquals(0, p.stonesLeft)
        assertEquals(0, p.numberOfPossibleTurns)
        assertEquals(0, p.totalPoints)
        assertNull(p.lastShape)
        assertNotNull(p.scores)

        assertEquals(PlayerScore(2, -1, 0, 0, 0, 0, false), p.scores)
    }
}
