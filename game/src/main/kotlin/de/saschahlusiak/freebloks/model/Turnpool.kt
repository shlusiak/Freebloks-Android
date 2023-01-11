package de.saschahlusiak.freebloks.model

import java.io.Serializable
import java.util.*

/**
 * Simple Stack to contain Turns for the undo history
 */
class Turnpool : LinkedList<Turn>(), Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}