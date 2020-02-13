package de.saschahlusiak.freebloks.model

/**
 * The end-of-game score of a player, which, in case of GAMEMODE_4_COLORS_2_PLAYERS may be
 * composed of two game players (e.g. red & blue).
 */
class PlayerScore @JvmOverloads constructor(game: Game, val player1: Int, val player2: Int = -1) : Comparable<PlayerScore> {
    val isLocal: Boolean = game.isLocalPlayer(player1)

    // the place will be set afterwards, when all points of all players are known
	var place = -1

    var points: Int
        private set

	var stonesLeft: Int
        private set

	var bonus = 0
        private set

    var isPerfect: Boolean
        private set

    init {
        isPerfect = false

        points = 0
        stonesLeft = 0

        addPoints(game.board.getPlayer(player1))
        // todo: make nullable instead
        if (player2 >= 0)
            addPoints(game.board.getPlayer(player2))
    }

    private fun addPoints(p: Player) {
        points += p.totalPoints
        stonesLeft += p.stonesLeft

        val lastPlayedShape = p.lastShape ?: return
        if (p.stonesLeft == 0) {
            if (lastPlayedShape.size == 1) {
                bonus += 20
                isPerfect = true
            } else {
                bonus += 15
                isPerfect = false
            }
        }
    }

    override fun compareTo(other: PlayerScore): Int {
        if (points > other.points) return -1
        if (points < other.points) return 1
        if (stonesLeft < other.stonesLeft) return -1
        return if (stonesLeft > other.stonesLeft) 1 else 0
    }
}