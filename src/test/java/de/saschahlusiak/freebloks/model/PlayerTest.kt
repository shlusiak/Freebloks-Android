package de.saschahlusiak.freebloks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {
    @Test
    fun test_constructor() {
        val p = Player(0)

        val stone = p.getStone(2)
        assertEquals(0, stone?.available)
    }

    // TODO
}
