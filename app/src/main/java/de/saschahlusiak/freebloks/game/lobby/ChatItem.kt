package de.saschahlusiak.freebloks.game.lobby

sealed class ChatItem {
    /**
     * Server message about a player (joined / left)
     *
     * @param player the player number, may not have a client associated
     * @param text the content of the message
     */
    data class Server(val player: Int, val text: String): ChatItem()

    /**
     * Chat message from a client
     *
     * @param client the client number
     * @param player the player number, may be null if the client has no player
     * @param isLocal true, if the player was a local player at the time the message was received
     * @param name the name of the player
     * @param text the content of the message
     */
    data class Message(val client: Int, val player: Int?, val isLocal: Boolean, val name: String, val text: String): ChatItem()

    /**
     * Generic message
     */
    data class Generic(val text: String): ChatItem()
}
