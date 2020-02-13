package de.saschahlusiak.freebloks.controller

import androidx.annotation.AnyThread
import de.saschahlusiak.freebloks.model.Spiel
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

/**
 * All callbacks happen on a background thread, usually the [SpielClientThread]
 */
interface GameObserver {
    @AnyThread
    fun onConnected(spiel: Spiel)

    @AnyThread
    fun onDisconnected(spiel: Spiel)

    @AnyThread
    fun newCurrentPlayer(player: Int)

    @AnyThread
    fun stoneWillBeSet(turn: Turn)

    @AnyThread
    fun stoneHasBeenSet(turn: Turn)

    @AnyThread
    fun hintReceived(turn: Turn)

    @AnyThread
    fun gameFinished()

    @AnyThread
    fun chatReceived(client: Int, message: String)

    @AnyThread
    fun gameStarted()

    @AnyThread
    fun stoneUndone(t: Turn)

    @AnyThread
    fun serverStatus(status: MessageServerStatus)
}
