package de.saschahlusiak.freebloks.model

import java.io.Serializable

fun GameMode.defaultBoardSize() = GameConfig.defaultSizeForMode(this)
fun GameMode.defaultStoneSet() = GameConfig.defaultStonesForMode(this)

data class GameConfig(
    val server: String?,
    val showLobby: Boolean,
    val requestPlayers: BooleanArray?, // 4
    val stones: IntArray, // 21
    val difficulty: Int,
    val gameMode: GameMode,
    val fieldSize: Int
) : Serializable {

    class Builder internal constructor() {
        private var server: String? = null
        private var showLobby: Boolean = false
        private var fieldSize: Int? = null
        private var gameMode: GameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS
        private var requestPlayers: BooleanArray? = null
        private var stones: IntArray? = null
        private var difficulty = DEFAULT_DIFFICULTY

        fun build(): GameConfig {
            return GameConfig(
                server,
                showLobby,
                requestPlayers,
                stones ?: gameMode.defaultStoneSet(),
                difficulty,
                gameMode,
                fieldSize ?: gameMode.defaultBoardSize()
            )
        }

        /**
         * @param server server to connect to or null for localhost (default)
         */
        fun server(server: String?): Builder {
            this.server = server
            return this
        }

        /**
         * @param showLobby show lobby (default false)
         */
        fun showLobby(showLobby: Boolean): Builder {
            this.showLobby = showLobby
            return this
        }

        /**
         * @param requestPlayers players to request; null for random (default)
         */
        fun requestPlayers(requestPlayers: BooleanArray?): Builder {
            this.requestPlayers = requestPlayers
            return this
        }

        /**
         * @param stones the availability of shapes, defaults to [defaultStonesForMode] if unset
         */
        fun stones(stones: IntArray): Builder {
            this.stones = stones
            return this
        }

        /**
         * @param fieldSize the size of the field, default 20
         */
        fun fieldSize(fieldSize: Int): Builder {
            this.fieldSize = fieldSize
            return this
        }

        /**
         * @param difficulty the difficulty, defaults to [DEFAULT_DIFFICULTY]
         */
        fun difficulty(difficulty: Int): Builder {
            this.difficulty = difficulty
            return this
        }

        /**
         * @param gameMode the new game mode
         */
        fun gameMode(gameMode: GameMode): Builder {
            this.gameMode = gameMode
            return this
        }
    }

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

        @JvmStatic
		fun builder(): Builder {
            return Builder()
        }
    }
}