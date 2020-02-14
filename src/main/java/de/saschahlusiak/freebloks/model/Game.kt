package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.model.Board.Companion.PLAYER_MAX
import java.io.Serializable
import java.util.*

/**
 * A "game" contains everything about the state of a game.
 *
 * These information are usually updated via the [NetworkEventHandler].
 *
 * @param board the board state
 */
class Game(val board: Board): Serializable {
    /**
     * The current player, or -1 if none.
     */
    var currentPlayer = -1

    /**
     * For each player, whether it is controlled locally or remotely (aka computer)
     */
    val playerTypes = IntArray(PLAYER_MAX) { PLAYER_COMPUTER }

    /**
     * The current game mode
     */
    var gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS

    /**
     * Whether the game is officially over
     */
    var isFinished = false

    /**
     * false if still in lobby, true if started
     */
    var isStarted = false

    /**
     * The history of [Turn]
     */
    val history = Turnpool()

    /**
     * @param player the player number to check or currentPlayer
     * @return true if the given player is played on the local device
     */
    @JvmOverloads
    fun isLocalPlayer(player: Int = currentPlayer): Boolean {
        return if (player == -1) false else playerTypes[player] != PLAYER_COMPUTER
    }

    /**
     * Set the given player number to either [PLAYER_COMPUTER] or [PLAYER_LOCAL].
     *
     * All remotely played users are from our point of view computer players.
     */
    fun setPlayerType(player: Int, playerType: Int) {
        playerTypes[player] = playerType
    }

    /**
     * Build and return the end-of-game scores.
     */
    fun getPlayerScores(): Array<PlayerScore> {
        val data: Array<PlayerScore>

        when (gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
            GameMode.GAMEMODE_DUO,
            GameMode.GAMEMODE_JUNIOR -> {
                data = arrayOf(
                    PlayerScore(this, 0),
                    PlayerScore(this, 2)
                )
            }
            GameMode.GAMEMODE_4_COLORS_2_PLAYERS -> {
                data = arrayOf(
                    PlayerScore(this, 0, 2),
                    PlayerScore(this, 1, 3)
                )
            }
            GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> {
                data = arrayOf(
                    PlayerScore(this, 0),
                    PlayerScore(this, 1),
                    PlayerScore(this, 2),
                    PlayerScore(this, 3)
                )
            }
        }

        Arrays.sort(data)
        // first everybody gets the natural place number
        data.forEachIndexed { index, p -> p.place = index + 1 }

        // then give everybody with equal points the same place
        for (i in 1 until data.size) {
            if (data[i].compareTo(data[i - 1]) == 0)
                data[i].place = data[i - 1].place
        }

        return data
    }

    companion object {
        const val PLAYER_COMPUTER = -2
        const val PLAYER_LOCAL = -1
    }
}