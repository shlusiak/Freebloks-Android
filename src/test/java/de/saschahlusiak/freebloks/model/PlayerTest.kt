package de.saschahlusiak.freebloks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {
    @Test
    fun test_constructor() {
        val p = Player()

        val stone = p.get_stone(2)
        assertEquals(0, stone._available)
    }

    // TODO
}
