package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.model.Board.Companion.FIELD_ALLOWED
import de.saschahlusiak.freebloks.model.Board.Companion.FIELD_DENIED
import de.saschahlusiak.freebloks.model.Board.Companion.FIELD_FREE
import de.saschahlusiak.freebloks.model.GameMode.*
import de.saschahlusiak.freebloks.utils.Point
import org.junit.Assert.*
import org.junit.Test

class BoardTest {
    private fun Player.getAllTurns(board: Board) = sequence {
        for (x in 0 until board.width) for (y in 0 until board.height) {
            if (board.getFieldStatus(number, y, x) == FIELD_ALLOWED) {
                stones.forEach {
                    if (it.isAvailable()) {
                        for (turn in board.getTurnsInPosition(number, it.shape, y, x)) yield(turn)
                    }
                }
            }
        }
    }

    private fun Board.playGame(picker: (List<Turn>) -> Turn): Turnpool {
        val turnPool = Turnpool()

        do {
            var moved = false
            for (number in 0..3) {
                val p = getPlayer(number)
                val turns = p.getAllTurns(this).toList()

                assertEquals(p.numberOfPossibleTurns, turns.size)

                if (turns.isNotEmpty()) {
                    val turn = picker.invoke(turns)
                    moved = true
                    turnPool.add(turn)
                    setStone(turn)
                }
            }
        } while (moved)

        return turnPool
    }

    /**
     * Sets the stone no 2 into the bottom left corner of a new game:
     *
     *  X
     * XX
     */
    @Test
    fun test_basic_stone() {
        val s = Board(20)

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(GameConfig.DEFAULT_STONE_SET)
        s.startNewGame(GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)

        assertEquals(20, s.width)
        assertEquals(20, s.height)

        val p = s.getPlayer(0)
        assertNotNull(p)

        assertEquals(58, p.numberOfPossibleTurns)

        val stone = p.getStone(2)
        assertEquals(1, stone.available)

        // start for blue is bottom left
        assertEquals(Point(0, 19), s.getPlayerSeed(0, GAMEMODE_4_COLORS_4_PLAYERS))

        // that field is allowed (green)
        assertEquals(FIELD_ALLOWED, s.getFieldStatus(0, 19, 0))
        // the field next to it is only free (blank)
        assertEquals(FIELD_FREE, s.getFieldStatus(0, 18, 0))
        // but there is no player on that field yet
        assertEquals(FIELD_FREE, s.getFieldPlayer(19, 0))

        assertFalse(s.isValidTurn(stone.shape, 0, 0, 0, Orientation.Default))

        //  X
        // XX
        val turn = Turn(0, stone.shape.number, 18, 0, Orientation(false, Rotation.Left))
        assertTrue(s.isValidTurn(turn))
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

        assertFalse(s.isValidTurn(turn))

        s.refreshPlayerData()
        assertEquals(141, s.getPlayer(0).numberOfPossibleTurns)
    }

    @Test(expected = GameStateException::class)
    fun test_single_stone_not_available() {
        val board = Board()
        val turn = Turn(0, 0, 0, 0, Orientation())
        board.setStone(turn)
    }

    @Test
    fun test_single_stone_next_to_each_other() {
        val board = Board()
        board.getPlayer(0).getStone(0).available = 2
        board.startNewGame(GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)
        board.refreshPlayerData()
        val turn1 = Turn(0, 0, 19, 0, Orientation())
        val turn2 = Turn(0, 0, 18, 0, Orientation())
        assertTrue(board.isValidTurn(turn1))
        board.setStone(turn1)
        assertFalse(board.isValidTurn(turn2))
        // well, setStone does not actually verify whether the turn is legal, so this
        // would succeed. Nothing to see here.
        board.setStone(turn2)
    }

    @Test(expected = GameStateException::class)
    fun test_single_stone_twice() {
        val board = Board()
        board.getPlayer(0).getStone(0).available = 2
        board.startNewGame(GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)
        board.refreshPlayerData()
        val turn = Turn(0, 0, 19, 0, Orientation())
        assertTrue(board.isValidTurn(turn))
        try {
            board.setStone(turn)
        } catch (e: GameStateException) {
            fail("Not expected here")
        }
        assertFalse(board.isValidTurn(turn))
        // setting the same stone twice throws an Exception
        board.setStone(turn)
    }

    @Test
    fun test_seed_duo() {
        val board = Board(14, 14)
        assertEquals(Point(4, 9), board.getPlayerSeed(0, GAMEMODE_DUO))
        assertEquals(Point(9, 4), board.getPlayerSeed(2, GAMEMODE_DUO))
    }

    @Test
    fun test_seed_junior() {
        val board = Board(14, 14)
        assertEquals(Point(4, 9), board.getPlayerSeed(0, GAMEMODE_JUNIOR))
        assertEquals(Point(9, 4), board.getPlayerSeed(2, GAMEMODE_JUNIOR))
    }

    @Test
    fun test_seed_classic() {
        val board = Board()
        assertEquals(Point(0, 19), board.getPlayerSeed(0, GAMEMODE_4_COLORS_4_PLAYERS))
        assertEquals(Point(0, 0), board.getPlayerSeed(1, GAMEMODE_4_COLORS_4_PLAYERS))
        assertEquals(Point(19, 0), board.getPlayerSeed(2, GAMEMODE_4_COLORS_4_PLAYERS))
        assertEquals(Point(19, 19), board.getPlayerSeed(3, GAMEMODE_4_COLORS_4_PLAYERS))
    }

    @Test
    fun test_seed_2_color_2_players() {
        val board = Board(GameConfig.defaultSizeForMode(GAMEMODE_2_COLORS_2_PLAYERS))
        assertEquals(Point(0, 14), board.getPlayerSeed(0, GAMEMODE_2_COLORS_2_PLAYERS))
        assertEquals(Point(0, 0), board.getPlayerSeed(1, GAMEMODE_2_COLORS_2_PLAYERS))
        assertEquals(Point(14, 0), board.getPlayerSeed(2, GAMEMODE_2_COLORS_2_PLAYERS))
        assertEquals(Point(14, 14), board.getPlayerSeed(3, GAMEMODE_2_COLORS_2_PLAYERS))
    }

    @Test
    fun test_full_game_4_4() {
        val s = Board(20)
        val mode = GAMEMODE_4_COLORS_4_PLAYERS

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(GameConfig.DEFAULT_STONE_SET)
        s.startNewGame(mode, 20, 20)
        s.refreshPlayerData()

        assertEquals(58, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(3).numberOfPossibleTurns)
        assertEquals(21, s.getPlayer(0).stonesLeft)
        assertEquals(21, s.getPlayer(1).stonesLeft)
        assertEquals(21, s.getPlayer(2).stonesLeft)
        assertEquals(21, s.getPlayer(3).stonesLeft)

        val turnpool = s.playGame { turns -> turns.last() }

        assertEquals(65, turnpool.size)
        assertEquals(3, s.getPlayer(0).stonesLeft)
        assertEquals(5, s.getPlayer(1).stonesLeft)
        assertEquals(4, s.getPlayer(2).stonesLeft)
        assertEquals(7, s.getPlayer(3).stonesLeft)

        assertEquals(78, s.getPlayer(0).totalPoints)
        assertEquals(69, s.getPlayer(1).totalPoints)
        assertEquals(75, s.getPlayer(2).totalPoints)
        assertEquals(61, s.getPlayer(3).totalPoints)

        assertEquals(0, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(3).numberOfPossibleTurns)

        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }
        assertEquals(58, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(3).numberOfPossibleTurns)
        assertEquals(21, s.getPlayer(0).stonesLeft)
        assertEquals(21, s.getPlayer(1).stonesLeft)
        assertEquals(21, s.getPlayer(2).stonesLeft)
        assertEquals(21, s.getPlayer(3).stonesLeft)
    }

    @Test
    fun test_full_game_duo() {
        val s = Board(15)
        val mode = GAMEMODE_DUO

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(GameConfig.DEFAULT_STONE_SET)
        s.startNewGame(mode, 15, 15)
        s.refreshPlayerData()

        assertEquals(309, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(309, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(3).numberOfPossibleTurns)
        assertEquals(21, s.getPlayer(0).stonesLeft)
        assertEquals(0, s.getPlayer(1).stonesLeft)
        assertEquals(21, s.getPlayer(2).stonesLeft)
        assertEquals(0, s.getPlayer(3).stonesLeft)

        val turnpool = s.playGame { turns -> turns.last() }

        assertEquals(34, turnpool.size)
        assertEquals(4, s.getPlayer(0).stonesLeft)
        assertEquals(0, s.getPlayer(1).stonesLeft)
        assertEquals(4, s.getPlayer(2).stonesLeft)
        assertEquals(0, s.getPlayer(3).stonesLeft)

        assertEquals(77, s.getPlayer(0).totalPoints)
        assertEquals(0, s.getPlayer(1).totalPoints)
        assertEquals(74, s.getPlayer(2).totalPoints)
        assertEquals(0, s.getPlayer(3).totalPoints)

        assertEquals(0, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(3).numberOfPossibleTurns)

        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }

        assertEquals(309, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(309, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(3).numberOfPossibleTurns)
        assertEquals(21, s.getPlayer(0).stonesLeft)
        assertEquals(0, s.getPlayer(1).stonesLeft)
        assertEquals(21, s.getPlayer(2).stonesLeft)
        assertEquals(0, s.getPlayer(3).stonesLeft)
    }

    @Test
    fun test_full_game_reversed() {
        val s = Board(20)
        val mode = GAMEMODE_4_COLORS_4_PLAYERS

        // we have to make stones available before starting a new game, otherwise we won't get seeds set
        s.setAvailableStones(GameConfig.DEFAULT_STONE_SET)
        s.startNewGame(GAMEMODE_4_COLORS_4_PLAYERS, 20, 20)
        s.refreshPlayerData()

        assertEquals(58, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(3).numberOfPossibleTurns)

        val turnpool = s.playGame { turns -> turns.first() }

        assertEquals(63, turnpool.size)
        assertEquals(6, s.getPlayer(0).stonesLeft)
        assertEquals(6, s.getPlayer(1).stonesLeft)
        assertEquals(4, s.getPlayer(2).stonesLeft)
        assertEquals(5, s.getPlayer(3).stonesLeft)

        assertEquals(59, s.getPlayer(0).totalPoints)
        assertEquals(59, s.getPlayer(1).totalPoints)
        assertEquals(69, s.getPlayer(2).totalPoints)
        assertEquals(64, s.getPlayer(3).totalPoints)

        assertEquals(0, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(0, s.getPlayer(3).numberOfPossibleTurns)


        // and backwards
        while (!turnpool.isEmpty()) {
            s.undo(turnpool, mode)
        }
        assertEquals(58, s.getPlayer(0).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(1).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(2).numberOfPossibleTurns)
        assertEquals(58, s.getPlayer(3).numberOfPossibleTurns)
        assertEquals(21, s.getPlayer(0).stonesLeft)
        assertEquals(21, s.getPlayer(1).stonesLeft)
        assertEquals(21, s.getPlayer(2).stonesLeft)
        assertEquals(21, s.getPlayer(3).stonesLeft)
    }
}