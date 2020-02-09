package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameMode
import de.saschahlusiak.freebloks.model.Stone.*
import org.junit.Assert.*
import org.junit.Test

class SpielTest {
    /**
     * Sets the stone no 2 into the bottom left corner of a new game:
     *
     *  X
     * XX
     */
    @Test
    fun test_basic_stone() {
        val s = Spiel(20)

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(1, 1, 1, 1, 1)
        s.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)
        s.refreshPlayerData()

        assertEquals(20, s.width)
        assertEquals(20, s.height)

        val p = s.getPlayer(0)
        assertNotNull(p)

        assertEquals(58, p.m_number_of_possible_turns)

        val stone = p.get_stone(2)
        assertNotNull(stone)
        assertEquals(1, stone.availableCount)

        // start for blue is bottom left
        assertEquals(0, s.getPlayerStartX(0))
        assertEquals(19, s.getPlayerStartY(0))

        // that field is allowed (green)
        assertEquals(FIELD_ALLOWED, s.getFieldStatus(0, 19, 0))
        // the field next to it is only free (blank)
        assertEquals(FIELD_FREE, s.getFieldStatus(0, 18, 0))
        // but there is no player on that field yet
        assertEquals(FIELD_FREE, s.getFieldPlayer(19, 0))

        assertEquals(FIELD_DENIED, s.isValidTurn(stone, 0, 0, 0, 0, 0))

        //  X
        // XX
        val turn = Turn(0, stone.shape, 18, 0, 0, 3)
        assertEquals(FIELD_ALLOWED, s.isValidTurn(turn))
        s.setStone(turn)
        assertEquals(0, stone.availableCount)
        assertEquals(0, s.getFieldPlayer(19, 0))
        assertEquals(0, s.getFieldPlayer(19, 1))
        assertEquals(FIELD_FREE, s.getFieldPlayer(18, 0))
        assertEquals(0, s.getFieldPlayer(18, 1))

        assertEquals(FIELD_DENIED, s.getFieldStatus(0, 19, 0))
        assertEquals(FIELD_DENIED, s.getFieldStatus(0, 19, 1))
        // even though 18/0 is free, for this player it is denied, because it shares an edge
        assertEquals(FIELD_DENIED, s.getFieldStatus(0, 18, 0))
        assertEquals(FIELD_DENIED, s.getFieldStatus(0, 18, 1))
        assertEquals(FIELD_ALLOWED, s.getFieldStatus(0, 17, 0))
        assertEquals(FIELD_DENIED, s.getFieldStatus(0, 17, 1))
        assertEquals(FIELD_ALLOWED, s.getFieldStatus(0, 17, 2))
        assertEquals(FIELD_FREE, s.getFieldStatus(0, 17, 3))

        assertEquals(FIELD_DENIED, s.isValidTurn(turn))

        s.refreshPlayerData()
        assertEquals(141, s.getPlayer(0).m_number_of_possible_turns)
    }
}