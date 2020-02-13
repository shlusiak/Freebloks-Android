package de.saschahlusiak.freebloks.controller

import de.saschahlusiak.freebloks.model.Spiel
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

interface SpielClientInterface {
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
