package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.model.GameStateException

interface MessageHandler {
    /**
     * Process a single message
     */
    @Throws(ProtocolException::class, GameStateException::class)
    fun handleMessage(message: Message)
}