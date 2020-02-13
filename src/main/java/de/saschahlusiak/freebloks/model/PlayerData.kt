package de.saschahlusiak.freebloks.model

class PlayerData @JvmOverloads constructor(spiel: Spielleiter, val player1: Int, val player2: Int = -1) : Comparable<PlayerData> {
    val isLocal: Boolean = spiel.is_local_player(player1)

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

        addPoints(spiel.getPlayer(player1))
        // todo: make nullable instead
        if (player2 >= 0)
            addPoints(spiel.getPlayer(player2))
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

    override fun compareTo(other: PlayerData): Int {
        if (points > other.points) return -1
        if (points < other.points) return 1
        if (stonesLeft < other.stonesLeft) return -1
        return if (stonesLeft > other.stonesLeft) 1 else 0
    }
}