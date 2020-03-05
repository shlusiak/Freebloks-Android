package de.saschahlusiak.freebloks.model

import java.io.Serializable

fun GameMode.defaultBoardSize() = GameConfig.defaultSizeForMode(this)
fun GameMode.defaultStoneSet() = GameConfig.defaultStonesForMode(this)

class GameConfig(
    val server: String? = null,
    val gameMode: GameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
    @JvmField val showLobby: Boolean = false,
    @JvmField val requestPlayers: BooleanArray? = null, // 4
    val difficulty: Int = DEFAULT_DIFFICULTY,
    val stones: IntArray = gameMode.defaultStoneSet(), // 21
    val fieldSize: Int = gameMode.defaultBoardSize()
) : Serializable {

    @Deprecated("use normal constructor")
    constructor(server: String?, showLobby: Boolean): this(server = server, showLobby = showLobby, gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS)

    companion object {
        /**
         * All shapes are available once (Blokus Classic)
         */
        val DEFAULT_STONE_SET = IntArray(Shape.COUNT) { 1 }

        /**
         * The simplified Junior shape set
         */
        val JUNIOR_STONE_SET = intArrayOf(
            2,
            2,
            2, 2,
            2, 2, 2, 2, 2,
            2, 0, 0, 0, 0, 0, 0, 0, 2, 2, 0, 0
        )

        /**
         * Default difficulty, higher is easier, lower is harder
         */
        const val DEFAULT_DIFFICULTY = 10

        /**
         * A list of possible field sizes for drop-down boxes.
         *
         * Matches the labels in R.array.game_field_sizes
         */
        @JvmField
        val FIELD_SIZES = intArrayOf(
            13, 14, 15, 17, 20, 23
        )

        /**
         * @return the availability numbers for each stone for the given game mode
         */
        @JvmStatic
        fun defaultStonesForMode(gameMode: GameMode) = when (gameMode) {
            GameMode.GAMEMODE_JUNIOR -> JUNIOR_STONE_SET

            else -> DEFAULT_STONE_SET
        }

        /**
         * The default board size for the given game mode
         */
        fun defaultSizeForMode(gameMode: GameMode) = when(gameMode) {
            GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> 20
            GameMode.GAMEMODE_4_COLORS_2_PLAYERS -> 20
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> 15
            GameMode.GAMEMODE_DUO -> 14
            GameMode.GAMEMODE_JUNIOR -> 14
        }
    }
}