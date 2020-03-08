package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.utils.Point
import java.io.Serializable
import java.lang.IllegalArgumentException

/**
 * Current state of the board.Contains:
 *
 * - field state
 * - players and their remaining stones
 *
 * Does not contain meta information about the game, like current player.
 * See [Game] for that.
 */
class Board(@JvmField var width: Int, @JvmField var height: Int) : Serializable {
    @JvmOverloads
    constructor(size: Int = DEFAULT_BOARD_SIZE) : this(size, size)

    /**
     * Encapsulated player information
     */
    private val player = Array(PLAYER_MAX) { Player(it) }

    /**
     * One dimensional board field [y * width + x]
     */
    var fields: IntArray = IntArray(width * height)
        private set

    /**
     * Mark field as FIELD_ALLOWED for given player, if field is free
     */
    private fun setSeed(point: Point, player: Int) {
        if (getFieldStatus(player, point.y, point.x) == FIELD_FREE)
            fields[point.y * width + point.x] = PLAYER_BIT_ALLOWED[player]
    }

    /**
     * @return The x-coordinate of the player's start corner
     */
    @Deprecated("Use getPlayerSeed instead")
    fun getPlayerSeedX(player: Int, gameMode: GameMode): Int {
        return getPlayerSeed(player, gameMode)?.x ?: throw IllegalArgumentException("Player $player has no seed")
    }

    /**
     * @return The y-coordinate of the player's start corner
     */
    @Deprecated("Use getPlayerSeed instead")
    fun getPlayerSeedY(player: Int, gameMode: GameMode): Int {
        return getPlayerSeed(player, gameMode)?.y ?: throw IllegalArgumentException("Player $player has no seed")
    }

    /**
     * @return the seed position for the given player
     */
    fun getPlayerSeed(player: Int, gameMode: GameMode): Point? {
        return if (gameMode == GameMode.GAMEMODE_DUO || gameMode == GameMode.GAMEMODE_JUNIOR) {
            when (player) {
                0 -> Point(4, height - 5)
                2 -> Point(width - 5, 4)
                else -> null
            }
        } else {
            when (player) {
                0 -> Point(0, height - 1)
                1 -> Point(0, 0)
                2 -> Point(width - 1, -0)
                3 -> Point(width - 1, height - 1)
                else -> null
            }
        }
    }

    /**
     * Set all seeds for given game mode. Called upon undo, so fields may still be occupied.
     */
    private fun setSeeds(gameMode: GameMode) {
        for (p in 0 until PLAYER_MAX) {
            val seed = getPlayerSeed(p, gameMode) ?: continue
            setSeed(seed, p)
        }
    }

    /**
     * Returns whether for a given player the given field is allowed, denied or free
     *
     * @return One of [FIELD_ALLOWED], [FIELD_DENIED] or [FIELD_FREE].
     */
    fun getFieldStatus(player: Int, y: Int, x: Int): Int {
        var value = fields[y * width + x]
        // if a field is occupied, it's denied
        if (value >= PLAYER_BIT_HAVE_MIN) return FIELD_DENIED
        value = value and PLAYER_BIT_ADDR[player]
        // otherwise it is either allowed, denied or free
        if (value == PLAYER_BIT_ALLOWED[player]) return FIELD_ALLOWED
        if (value == PLAYER_BIT_DENIED[player]) return FIELD_DENIED
        return FIELD_FREE
    }

    /**
     * @return The value of the player occupying the field or FIELD_FREE
     */
    fun getFieldPlayer(y: Int, x: Int): Int {
        val value = fields[y * width + x]
        return if (value < PLAYER_BIT_HAVE_MIN) FIELD_FREE else value and 3
    }

    fun getFieldPlayer(point: Point) = getFieldPlayer(point.y, point.x)

    /**
     * @return Player instance for given player
     */
    fun getPlayer(number: Int): Player {
        return player[number]
    }

    /**
     * Sets the number of available stones for each stone.
     *
     * @param stoneAvailability for each shape, the number of available stones
     */
    fun setAvailableStones(stoneAvailability: IntArray) {
        player.forEach { player ->
            for (n in 0 until Shape.COUNT) {
                player.getStone(n).available = stoneAvailability[n]
            }
        }
        refreshPlayerData()
    }

    /**
     * Initialise board and player state
     */
    @JvmOverloads
    fun startNewGame(gameMode: GameMode, width: Int = this.width, height: Int = this.height) {
        this.width = width
        this.height = height
        fields = IntArray(this.width * this.height)
        when (gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
            GameMode.GAMEMODE_DUO,
            GameMode.GAMEMODE_JUNIOR -> {
                var n = 0
                while (n < Shape.COUNT) {
                    getPlayer(1).getStone(n).available = 0
                    getPlayer(3).getStone(n).available = 0
                    n++
                }
            }
            GameMode.GAMEMODE_4_COLORS_2_PLAYERS,
            GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> {
            }
        }

        setSeeds(gameMode)
        refreshPlayerData()
    }

    /**
     * Refresh player metadata for current board state, like number of possible turns, etc.
     */
    internal fun refreshPlayerData() {
        player.forEach { it.scores = calculatePlayerScore(it.number) }
    }

    fun calculatePlayerScore(playerNumber: Int): PlayerScore {
        val player = getPlayer(playerNumber)
        var totalPoints = 0
        var numberOfPossibleTurns = 0
        var bonus = 0
        var isPerfect = false
        val stonesLeft = player.stones.sumBy { it.available }

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (getFieldStatus(player.number, y, x) == FIELD_ALLOWED) {
                    var turnsInPosition = 0
                    player.stones
                        .filter { it.isAvailable() }
                        .forEach {
                            val turns = getTurnsInPosition(player.number, it.shape, y, x)
                            turnsInPosition += turns.count()
                        }

                    numberOfPossibleTurns += turnsInPosition

                    if (turnsInPosition == 0) {
                        // FIXME: this doesn't belong in here
                        /* there is no available turn in this position. mark as not allowed */
                        clearAllowedBit(player.number, y, x)
                    }
                } else if (getFieldPlayer(y, x) == player.number) totalPoints++
            }
        }

        val lastStone = player.lastShape
        if (stonesLeft == 0 && lastStone != null) {
            bonus = 15
            if (lastStone.size == 1) {
                bonus += 5
                isPerfect = true
            }
        }

        totalPoints += bonus

        return PlayerScore(
            color1 = player.number,
            totalPoints = totalPoints,
            stonesLeft = stonesLeft,
            turnsLeft = numberOfPossibleTurns,
            bonus = bonus,
            isPerfect = isPerfect
        )
    }

    /**
     * Check whether the given stone/player/position is a valid move.
     *
     * @return true if allowed, false otherwise
     */
    fun isValidTurn(stone: Shape, player: Int, startY: Int, startX: Int, orientation: Orientation): Boolean {
        var valid = false
        for (y in 0 until stone.size) {
            for (x in 0 until stone.size) {
                if (stone.isStone(x, y, orientation)) {
                    if (y + startY < 0 || y + startY >= height || x + startX < 0 || x + startX >= width)
                        return false
                    val fieldValue = getFieldStatus(player, y + startY, x + startX)
                    if (fieldValue == FIELD_DENIED) return false
                    if (fieldValue == FIELD_ALLOWED) valid = true
                }
            }
        }
        return valid
    }

    /**
     * Check whether the given stone/player/position is a valid move.
     */
    fun isValidTurn(turn: Turn): Boolean {
        return isValidTurn(turn.shape, turn.player, turn.y, turn.x, turn.orientation)
    }

    /**
     * Clear the given field (set to 0) [FIELD_FREE]
     */
    private fun clearField(x: Int, y: Int) {
        fields[y * width + x] = 0
    }

    /**
     * Marks a single field as owned by a player.
     *
     * 1. mark that field as owned
     * 2. mark edges as denied
     * 3. mark corners as allowed (unless already denied)
     */
    @Throws(GameStateException::class)
    private fun setSingleStone(player: Int, fieldY: Int, fieldX: Int) {
        if (getFieldPlayer(fieldY, fieldX) != FIELD_FREE) throw GameStateException("field already set")
        fields[fieldY * width + fieldX] = PLAYER_BIT_HAVE_MIN or player
        for (y in (fieldY - 1)..(fieldY + 1)) if (y in 0 until height) {
            for (x in (fieldX - 1)..(fieldX + 1)) if (x in 0 until width) {
                if (getFieldStatus(player, y, x) != FIELD_DENIED) {
                    val idx = y * width + x
                    if (y != fieldY && x != fieldX) {
                        // mark the corners as allowed (unless already denied)
                        fields[idx] = fields[idx] or PLAYER_BIT_ALLOWED[player]
                    } else {
                        // mark the edges as denied
                        fields[idx] = fields[idx] and PLAYER_BIT_ALLOWED[player].inv()
                        fields[idx] = fields[idx] or PLAYER_BIT_DENIED[player]
                    }
                }
            }
        }
    }

    /**
     * Clear the PLAYER_BIT_ALLOWED bit, because there is no possible valid turn for this field.
     */
    fun clearAllowedBit(player: Int, y: Int, x: Int) {
        fields[y * width + x] = fields[y * width + x] and PLAYER_BIT_ALLOWED[player].inv()
    }

    /**
     * Execute a Turn and place the stone on the field
     */
    @Throws(GameStateException::class)
    fun setStone(turn: Turn) {
        val player = getPlayer(turn.player)
        val shape = turn.shape
        setStone(player.getStone(shape.number), turn.player, turn.y, turn.x, turn.orientation)
    }

    /**
     * Places given shape onto field.
     *
     * Does not perform verification, see [.isValidTurn] instead
     */
    @Throws(GameStateException::class)
    private fun setStone(stone: Stone, player: Int, startY: Int, startX: Int, orientation: Orientation) {
        val shape = stone.shape
        for (y in 0 until shape.size) {
            for (x in 0 until shape.size) {
                if (shape.isStone(x, y, orientation)) {
                    setSingleStone(player, startY + y, startX + x)
                }
            }
        }
        this.player[player].lastShape = shape
        stone.availableDecrement()
        refreshPlayerData()
    }

    fun getTurnsInPosition(player: Int, shape: Shape, fieldY: Int, fieldX: Int) = sequence {
        for (orientation in shape.orientations) {
            for (x in 0 until shape.size) {
                for (y in 0 until shape.size) {
                    if (shape.isCorner(x, y, orientation)) {
                        if (isValidTurn(shape, player, fieldY - y, fieldX - x, orientation)) {
                            yield(Turn(player, shape, orientation, fieldY - y, fieldX - x))
                        }
                    }
                }
            }
        }
    }

    /**
     * Undo the last turn in the given turnPool.
     */
    @Throws(GameStateException::class)
    fun undo(turnPool: Turnpool, gameMode: GameMode) {
        val turn = turnPool.pollLast()
            ?: throw GameStateException("Undo requested, but no last turn stored")
        val shape = turn.shape

        var y: Int
        var x = 0
        // remove stone
        while (x < shape.size) {
            y = 0
            while (y < shape.size) {
                if (shape.isStone(x, y, turn.orientation)) {
                    if (getFieldPlayer(turn.y + y, turn.x + x) == FIELD_FREE) throw GameStateException("field is free but shouldn't")
                    clearField(turn.x + x, turn.y + y)
                }
                y++
            }
            x++
        }
        // clear all markers for the entire board
        x = 0
        while (x < width) {
            y = 0
            while (y < height) {
                if (getFieldPlayer(y, x) == FIELD_FREE) {
                    clearField(x, y)
                }
                y++
            }
            x++
        }
        // place all existing stones again to recreate the markers
        x = 0
        while (x < width) {
            y = 0
            while (y < height) {
                if (getFieldPlayer(y, x) != FIELD_FREE) {
                    val player = getFieldPlayer(y, x)
                    clearField(x, y)
                    setSingleStone(player, y, x)
                }
                y++
            }
            x++
        }
        // try to set all seeds again, in case we cleared up the starting points
        setSeeds(gameMode)
        val stone = player[turn.player].getStone(shape.number)
        stone.availableIncrement()
        refreshPlayerData()
    }

    companion object {
        const val FIELD_FREE = 240
        const val FIELD_ALLOWED = 241
        const val FIELD_DENIED = 255
        const val PLAYER_MAX = 4
        const val DEFAULT_BOARD_SIZE = 20

        /**
         * A field is encoded in 8 bits, or 2 bits per player.
         *
         *
         * No bit set: FIELD_FREE
         * first bit set: FIELD_ALLOWED (shares a corner)
         * second bit set: FIELD_DENIED (shares an edge)
         *
         *
         * if first 6 bits are set, the lower most two bits encode the player number occupying the field
         */
        private val PLAYER_BIT_ADDR = intArrayOf(
            0x01 or 0x02,  // 00000011b
            0x04 or 0x08,  // 00001100b
            0x10 or 0x20,  // 00110000b
            0x40 or 0x80 // 11000000b
        )

        /**
         * Bit mask to filter for allowed fields
         */
        private val PLAYER_BIT_ALLOWED = intArrayOf(
            0x01,  // 00000001b
            0x04,  // 00000100b
            0x10,  // 00010000b
            0x40 // 01000000b
        )

        /**
         * Bit mask to filter for denied fields
         */
        private val PLAYER_BIT_DENIED = intArrayOf(
            0x02,  // 00000010b
            0x08,  // 00001000b
            0x20,  // 00100000b
            0x80 // 10000000b
        )

        /**
         * If field value has first 6 bits set, the lower two bits encode the player number owning the field
         */
        private const val PLAYER_BIT_HAVE_MIN = 252
    }
}