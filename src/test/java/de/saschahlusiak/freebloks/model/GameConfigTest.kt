package de.saschahlusiak.freebloks.model

import org.junit.Assert.*
import org.junit.Test

class GameConfigTest {
    @Test
    fun test_configuration_default() {
        val config = GameConfig()

        assertNotNull(config)
        assertNull(config.server)
        assertNull(config.requestPlayers)
        assertEquals(GameConfig.DEFAULT_DIFFICULTY, config.difficulty)
        assertEquals(GameConfig.DEFAULT_STONE_SET, config.stones)
        assertEquals(Board.DEFAULT_BOARD_SIZE, config.fieldSize)
        assertEquals(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, config.gameMode)
        assertEquals(false, config.showLobby)
    }

    @Test
    fun test_configuration_values() {
        val config = GameConfig(
            server = "server",
            gameMode = GameMode.GAMEMODE_JUNIOR,
            showLobby = true,
            fieldSize = 10,
            difficulty = 17,
            requestPlayers = booleanArrayOf(true, false, true, false)
        )

        assertNotNull(config)
        assertEquals("server", config.server)
        assertArrayEquals(booleanArrayOf(true, false, true, false), config.requestPlayers)
        assertEquals(17, config.difficulty)
        assertEquals(GameConfig.JUNIOR_STONE_SET, config.stones)
        assertEquals(10, config.fieldSize)
        assertEquals(GameMode.GAMEMODE_JUNIOR, config.gameMode)
        assertEquals(true, config.showLobby)
    }

    @Test
    fun test_configuration_stones_override_junior() {
        val config = GameConfig(
            gameMode = GameMode.GAMEMODE_JUNIOR,
            stones = IntArray(21) { 7 }
        )

        assertNotNull(config)
        assertArrayEquals(IntArray(21) { 7 }, config.stones)
        assertEquals(14, config.fieldSize)
        assertEquals(GameMode.GAMEMODE_JUNIOR, config.gameMode)
    }

    @Test
    fun test_configuration_2_players() {
        val config = GameConfig(gameMode = GameMode.GAMEMODE_2_COLORS_2_PLAYERS)

        assertEquals(15, config.fieldSize)
        assertEquals(GameConfig.DEFAULT_STONE_SET, config.stones)
    }
}