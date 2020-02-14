package de.saschahlusiak.freebloks.model

import java.io.Serializable

/**
 * The current scores of a player.
 * This may be a combined score of two colors
 */
data class PlayerScore(
    val color1: Int,
    val color2: Int = -1,
    val totalPoints: Int,
    val stonesLeft: Int,
    val turnsLeft: Int,
    val bonus: Int,
    val isPerfect: Boolean
) : Serializable, Comparable<PlayerScore> {

    // the place and local player information will be set afterwards, when all points of all players are known
    var place = -1
    var isLocal = false

    /**
     * Combine the scores of two colors into one
     */
    constructor(color1: PlayerScore, color2: PlayerScore) :
        this(color1.color1, color2.color1,
            color1.totalPoints + color2.totalPoints,
            color1.stonesLeft + color2.stonesLeft,
            color1.turnsLeft + color2.turnsLeft,
            color1.bonus + color2.bonus,
            color2.isPerfect
        )

    override fun compareTo(other: PlayerScore): Int {
        if (totalPoints > other.totalPoints) return -1
        if (totalPoints < other.totalPoints) return 1
        if (stonesLeft < other.stonesLeft) return -1
        return if (stonesLeft > other.stonesLeft) 1 else 0
    }
}