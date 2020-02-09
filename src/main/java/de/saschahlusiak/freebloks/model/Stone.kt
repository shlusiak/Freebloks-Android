package de.saschahlusiak.freebloks.model

import de.saschahlusiak.freebloks.controller.GameStateException
import java.io.Serializable

/**
 * A [Shape] with an availability count to be used in [Player]
 */
class Stone(val shape: Shape, var available: Int = 0) : Serializable {
    constructor(type: Int, available: Int = 0) : this(Shape.get(type), available)

    fun isAvailable() = available > 0

    fun availableIncrement() {
        available++
    }

    @Throws(GameStateException::class)
    fun availableDecrement() {
        if (available <= 0) throw GameStateException("stone " + shape.number + " not available")
        available--
    }
}