package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.model.Spiel
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

/**
 * All callbacks happen on a background thread!
 *
 * This is usually the [SpielClientThread].
 *
 * Register with [NetworkEventHandler] to get updates.
 */
interface GameObserver {
    fun onConnected(spiel: Spiel)
    fun onDisconnected(spiel: Spiel)
    fun newCurrentPlayer(player: Int)
    fun stoneWillBeSet(turn: Turn)
    fun stoneHasBeenSet(turn: Turn)
    fun hintReceived(turn: Turn)
    fun gameFinished()
    fun chatReceived(client: Int, message: String)
    fun gameStarted()
    fun stoneUndone(t: Turn)
    fun serverStatus(status: MessageServerStatus)
}
