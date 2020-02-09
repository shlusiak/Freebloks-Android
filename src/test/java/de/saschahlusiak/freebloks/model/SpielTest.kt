package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameMode
import de.saschahlusiak.freebloks.model.Spiel.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalStateException

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
        assertEquals(1, stone.available)

        // start for blue is bottom left
        assertEquals(0, s.getPlayerStartX(0))
        assertEquals(19, s.getPlayerStartY(0))

        // that field is allowed (green)
        assertEquals(FIELD_ALLOWED, s.getFieldStatus(0, 19, 0))
        // the field next to it is only free (blank)
        assertEquals(FIELD_FREE, s.getFieldStatus(0, 18, 0))
        // but there is no player on that field yet
        assertEquals(FIELD_FREE, s.getFieldPlayer(19, 0))

        assertEquals(FIELD_DENIED, s.isValidTurn(stone.shape, 0, 0, 0, 0, 0))

        //  X
        // XX
        val turn = Turn(0, stone.shape.number, 18, 0, 0, 3)
        assertEquals(FIELD_ALLOWED, s.isValidTurn(turn))
        s.setStone(turn)
        assertEquals(0, stone.available)
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

    private fun findTurn(spiel: Spiel, player: Int, reversed: Boolean): Turn? {
        val p = spiel.getPlayer(player)
        if (p.m_number_of_possible_turns == 0) return null

        var availableStones = p.m_stone.filter { it.isAvailable() }
        if (reversed) availableStones = availableStones.asReversed()

        for (x in 0 until spiel.width) for (y in 0 until spiel.height) {
            if (spiel.getFieldStatus(player, y, x) != FIELD_ALLOWED) continue

            availableStones.forEach { stone ->
                val shape = stone.shape
                shape.orientations.forEach { orientation ->

                    for (sx in 0 until shape.size) for (sy in 0 until shape.size) {
                        val turn = Turn(player, shape.number, y - sy, x - sx, orientation)
                        if (spiel.isValidTurn(turn) == FIELD_ALLOWED) return turn
                    }
                }
            }
        }
        throw IllegalStateException("Turn not found")
    }

    @Test
    fun test_full_game_4_4() {
        val s = Spiel(20)
        val turnpool = Turnpool()
        val mode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(1, 1, 1, 1, 1)
        s.startNewGame(mode, 20, 20)
        s.refreshPlayerData()

        assertEquals(58, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(3).m_number_of_possible_turns)
        assertEquals(21, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(21, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)

        do {
            var moved = false
            for (player in 0 until 4) {
                val turn = findTurn(s, player, true)

                if (turn != null ) {
                    moved = true
                    turnpool.add(turn)
                    s.setStone(turn)
                }
            }
        } while (moved)

        assertEquals(59, turnpool.size)
        assertEquals(7, s.getPlayer(0).m_stone_count)
        assertEquals(9, s.getPlayer(1).m_stone_count)
        assertEquals(7, s.getPlayer(2).m_stone_count)
        assertEquals(2, s.getPlayer(3).m_stone_count)

        assertEquals(0, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(3).m_number_of_possible_turns)

        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }
        assertEquals(58, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(3).m_number_of_possible_turns)
        assertEquals(21, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(21, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)
    }

    @Test
    fun test_full_game_duo() {
        val s = Spiel(15)
        val turnpool = Turnpool()
        val mode = GameMode.GAMEMODE_DUO

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(1, 1, 1, 1, 1)
        s.startNewGame(mode, 15, 15)
        s.refreshPlayerData()

        assertEquals(309, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(309, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(3).m_number_of_possible_turns)
        assertEquals(21, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(21, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)

        do {
            var moved = false
            for (player in 0 until 4) {
                val turn = findTurn(s, player, true)

                if (turn != null ) {
                    moved = true
                    turnpool.add(turn)
                    s.setStone(turn)
                }
            }
        } while (moved)

        assertEquals(40, turnpool.size)
        assertEquals(2, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(0, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)

        assertEquals(0, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(3).m_number_of_possible_turns)

        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }

        assertEquals(309, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(309, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(3).m_number_of_possible_turns)
        assertEquals(21, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(21, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)
    }

    @Test
    fun test_full_game_reversed() {
        val s = Spiel(20)
        val turnpool = Turnpool()
        val mode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(1, 1, 1, 1, 1)
        s.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)
        s.refreshPlayerData()

        assertEquals(58, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(3).m_number_of_possible_turns)

        do {
            var moved = false
            for (player in 0 until 4) {
                val turn = findTurn(s, player, false)

                if (turn != null ) {
                    moved = true
                    turnpool.add(turn)
                    s.setStone(turn)
                }
            }
        } while (moved)

        assertEquals(65, turnpool.size)
        assertEquals(5, s.getPlayer(0).m_stone_count)
        assertEquals(10, s.getPlayer(1).m_stone_count)
        assertEquals(3, s.getPlayer(2).m_stone_count)
        assertEquals(1, s.getPlayer(3).m_stone_count)

        assertEquals(0, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(0, s.getPlayer(3).m_number_of_possible_turns)


        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }
        assertEquals(58, s.getPlayer(0).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(1).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(2).m_number_of_possible_turns)
        assertEquals(58, s.getPlayer(3).m_number_of_possible_turns)
        assertEquals(21, s.getPlayer(0).m_stone_count)
        assertEquals(21, s.getPlayer(1).m_stone_count)
        assertEquals(21, s.getPlayer(2).m_stone_count)
        assertEquals(21, s.getPlayer(3).m_stone_count)
    }
}