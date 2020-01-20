package de.saschahlusiak.freebloks.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TurnTest {
    @Test
    fun test_constructor() {
        val t = Turn(1, 2, 3, 4, 5, 6)

        assertEquals(1, t.m_playernumber)
        assertEquals(2, t.m_stone_number)
        assertEquals(3, t.m_y)
        assertEquals(4, t.m_x)
        assertEquals(5, t.m_mirror_count)
        assertEquals(6, t.m_rotate_count)
    }

    @Test
    fun test_copy_constructor() {
        val other = Turn(1, 2, 3, 4, 5, 6)
        val t = Turn(other)

        assertEquals(1, t.m_playernumber)
        assertEquals(2, t.m_stone_number)
        assertEquals(3, t.m_y)
        assertEquals(4, t.m_x)
        assertEquals(5, t.m_mirror_count)
        assertEquals(6, t.m_rotate_count)
    }
}