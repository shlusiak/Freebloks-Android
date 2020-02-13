package de.saschahlusiak.freebloks.model

import java.io.Serializable

/**
 * A player within a game. Has 21 [Stone] with different availability and cached values.
 */
class Player(val number: Int) : Serializable {
    /**
     * Stones with availability for this player.
     *
     * Every player always has 21 stones with different availability.
     */
    val stones = Array(Shape.COUNT) { index ->
        Stone(index, 0)
    }

    /**
     * The last played shape or null if none or unknown
     */
    var lastShape: Shape? = null

    /**
     * The positive number of points for this player so far.
     *
     * This equals to the number of single squares occupied on the field, plus an optional
     * bonus of either 15 or 20 points if all stones are used.
     *
     * Refreshed in [refreshData]
     */
    var totalPoints = 0
        private set

    /**
     * The number of individual stones left for this player.
     *
     * Refreshed in [refreshData]
     */
    var stonesLeft = 0
        private set

    /**
     * The number of all posible [Turn] for this player
     *
     * Refreshed in [refreshData]
     */
    var numberOfPossibleTurns = 0
        private set

    fun getStone(n: Int) = stones[n]

    fun refreshData(board: Board) {
        totalPoints = 0
        numberOfPossibleTurns = 0
        stonesLeft = 0
        for (n in 0 until Shape.COUNT) {
            val stone = stones[n]
            stonesLeft += stone.available
        }
        for (x in 0 until board.width) {
            for (y in 0 until board.height) {
                if (board.getFieldStatus(number, y, x) == Board.FIELD_ALLOWED) {
                    var turnsInPosition = 0
                    stones
                        .filter { it.isAvailable() }
                        .forEach {
                            val turns = getTurnsInPosition(board, it.shape, y, x)
                            turnsInPosition += turns.count()
                        }

                    numberOfPossibleTurns += turnsInPosition
                    if (turnsInPosition == 0) { /* there is no available turn in this position. mark as not allowed */
                        board.clearAllowedBit(number, y, x)
                    }
                } else if (board.getFieldPlayer(y, x) == number) totalPoints++
            }
        }

        val lastStone = lastShape
        if (stonesLeft == 0 && lastStone != null) {
            totalPoints += 15
            if (lastStone.number == 0) totalPoints += 5
        }
    }

    private fun getTurnsInPosition(board: Board, shape: Shape, fieldY: Int, fieldX: Int) = sequence {
        for (orientation in shape.orientations) {
            for (x in 0 until shape.size) {
                for (y in 0 until shape.size) {
                    if (shape.isCorner(x, y, orientation)) {
                        if (board.isValidTurn(shape, number, fieldY - y, fieldX - x, orientation) == Board.FIELD_ALLOWED) {
                            yield(Turn(number, shape, orientation, fieldY - y, fieldX - x))
                        }
                    }
                }
            }
        }
    }

    fun getAllTurns(board: Board) = sequence {
        for (x in 0 until board.width) for (y in 0 until board.height) {
            if (board.getFieldStatus(number, y, x) == Board.FIELD_ALLOWED) {
                stones
                    .forEach {
                        if (it.isAvailable()) {
                            for (turn in getTurnsInPosition(board, it.shape, y, x)) yield(turn)
                        }
                    }
            }
        }
    }
}