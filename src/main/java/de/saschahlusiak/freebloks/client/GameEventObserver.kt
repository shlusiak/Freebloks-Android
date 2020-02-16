package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

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
    fun chatReceived(client: Int, message: String) {}
    fun gameStarted() {}
    fun stoneUndone(t: Turn) {}
    fun serverStatus(status: MessageServerStatus) {}
}
