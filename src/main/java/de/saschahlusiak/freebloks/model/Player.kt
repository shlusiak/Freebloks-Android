package de.saschahlusiak.freebloks.model

import java.io.Serializable

/**
 * A player within a game.
 *
 * - Contains 21 [Stone] with availability,
 * - last played stone
 * - cached game metadata like number of turns
 */
class Player(val number: Int) : Serializable {
    /**
     * Stones with availability for this player.
     *
     * Every player always has 21 stones with different availability.
     *
     * Defaults to 0
     */
    val stones = Array(Shape.COUNT) { index ->
        Stone(index, 0)
    }

    /**
     * The last played shape or null if none or unknown
     */
    var lastShape: Shape? = null

    /**
     * The last calculated scores of this player
     */
    var scores = PlayerScore(number, -1, 0, 0, 0, 0, false)

    val totalPoints get() = scores.totalPoints
    val stonesLeft get() = scores.stonesLeft
    val numberOfPossibleTurns get() = scores.turnsLeft

    fun getStone(n: Int) = stones[n]
}