package de.saschahlusiak.freebloks.client

import androidx.annotation.UiThread
import de.saschahlusiak.freebloks.model.Player
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

/**
 * Register with [GameClientMessageHandler] to get updates.
 */
interface GameEventObserver {
    /**
     * The first message ever received; we are connected to a client.
     */
    @UiThread
    fun onConnected(client: GameClient) {}

    /**
     * The connection attempt has failed with the given error.
     *
     * @param client the [GameClient]
     * @param error the final error of the connection attempt
     */
    @UiThread
    fun onConnectionFailed(client: GameClient, error: Exception) {}

    /**
     * The last message ever received; we are disconnected.
     *
     * @param client the GameClient
     * @param error optional error received while we were connected
     */
    @UiThread
    fun onDisconnected(client: GameClient, error: Throwable?) {}

    @UiThread fun newCurrentPlayer(player: Int) {}
    @UiThread fun stoneWillBeSet(turn: Turn) {}
    @UiThread fun stoneHasBeenSet(turn: Turn) {}
    @UiThread fun playerIsOutOfMoves(player: Player) {}
    @UiThread fun hintReceived(turn: Turn) {}
    @UiThread fun gameFinished() {}
    @UiThread fun gameStarted() {}
    @UiThread fun stoneUndone(t: Turn) {}
    @UiThread fun serverStatus(status: MessageServerStatus) {}

    /**
     * Player may be -1 if client has no player
     * TODO: make nullable instead
     */
    @UiThread fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {}

    /**
     * Player has joined.
     *
     * @param client the new client number
     * @param player the color the player is playing
     * @param name the name of the client playing this player
     */
    @UiThread fun playerJoined(client: Int, player: Int, name: String?) {}

    /**
     * Player has left
     *
     * @param client the client number that was playing that color
     * @param player the player the client was playing
     * @param name the name that client had
     */
    @UiThread fun playerLeft(client: Int, player: Int, name: String?) {}
}
