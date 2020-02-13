package de.saschahlusiak.freebloks.model

import java.io.Serializable
import java.util.*

/**
 * Enriches the current board state with meta information about the game:
 *
 * - game state
 * - history for undo
 * - current player
 * - whether players are controlled locally or by the computer
 * - current game mode
 */
class Spielleiter(size: Int) : Spiel(size), Serializable {
    // -1 is "no current player", used in between states
    var currentPlayer = -1

    @JvmField
    val spieler = IntArray(PLAYER_MAX) { PLAYER_COMPUTER }
    var gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS
    var isFinished = false
    var isStarted = false
    val history = Turnpool()

    /**
     * @param player the player number to check or currentPlayer
     * @return true if the given player is played on the local device
     */
    @JvmOverloads
    fun isLocalPlayer(player: Int = currentPlayer): Boolean {
        return if (player == -1) false else spieler[player] != PLAYER_COMPUTER
    }

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