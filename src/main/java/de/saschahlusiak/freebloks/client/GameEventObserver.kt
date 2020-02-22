package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.network.MessageReadThread

/**
 * All callbacks happen on a background thread!
 *
 * This is usually the [MessageReadThread].
 *
 * Register with [GameClientMessageHandler] to get updates.
 */
interface GameEventObserver {
    /**
     * The first message ever received; we are connected to a client.
     */
    fun onConnected(client: GameClient) {}

    /**
     * The last message ever received; we are disconnected.
     */
    fun onDisconnected(client: GameClient, error: Exception?) {}

    fun newCurrentPlayer(player: Int) {}
    fun stoneWillBeSet(turn: Turn) {}
    fun stoneHasBeenSet(turn: Turn) {}
    fun hintReceived(turn: Turn) {}
    fun gameFinished() {}
    fun gameStarted() {}
    fun stoneUndone(t: Turn) {}
    fun serverStatus(status: MessageServerStatus) {}

    /**
     * Player may be -1 if client has no player
     * TODO: make nullable instead
     */
    fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {}

    /**
     * Player has joined.
     *
     * @param client the new client number
     * @param player the color the player is playing
     * @param name the name of the client playing this player
     */
    fun playerJoined(client: Int, player: Int, name: String?) {}

    /**
     * Player has left
     *
     * @param client the client number that was playing that color
     * @param player the player the client was playing
     * @param name the name that client had
     */
    fun playerLeft(client: Int, player: Int, name: String?) {}
}
