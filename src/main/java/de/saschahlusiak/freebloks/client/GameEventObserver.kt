package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

/**
 * All callbacks happen on a background thread!
 *
 * This is usually the [GameClientThread].
 *
 * Register with [NetworkEventHandler] to get updates.
 */
interface GameEventObserver {
    fun onConnected(board: Board)
    fun onDisconnected(board: Board)
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
