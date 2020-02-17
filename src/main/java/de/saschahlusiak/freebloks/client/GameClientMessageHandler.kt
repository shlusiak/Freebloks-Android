package de.saschahlusiak.freebloks.client

import android.util.Log
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.lobby.ChatEntry
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageHandler
import de.saschahlusiak.freebloks.network.ProtocolException
import de.saschahlusiak.freebloks.network.message.*
import java.lang.ref.WeakReference

/**
 * Processes incoming network events and applies changes to the given [Game] and notifies [GameEventObserver].
 */
class GameClientMessageHandler(private val game: Game): MessageHandler {
    private val tag = GameClientMessageHandler::class.java.simpleName

    private val observer = mutableListOf<WeakReference<GameEventObserver>>()

    private val board = game.board

    /**
     * This is to be able to compare when players join or leave the game
     */
    private var lastStatus: MessageServerStatus? = null

    /**
     * Adds the given [GameEventObserver]. Note that it will be stored as a WeakReference.
     *
     * Observers are best added before the call to [GameClient.connected], so no events are missed.
     *
     * @param observer a new observer.
     */
    fun addObserver(observer: GameEventObserver) {
        synchronized(observer) {
            this.observer.add(WeakReference(observer))
        }
    }

    fun removeObserver(observer: GameEventObserver) {
        // we just clear the matching referents, but won't modify the list here
        // to avoid the concurrent modification exception
        synchronized(observer) {
            this.observer.filter { it.get() == observer }.forEach { it.clear() }
        }
    }

    private fun notifyObservers(block: (GameEventObserver) -> Unit) {
        synchronized(observer) {
            observer.forEach {
                val o = it.get()
                if (o != null) block.invoke(o)
            }
            // clean up all cleared referents
            observer.removeAll { it.get() == null }
        }
    }

    /**
     * Notify all observers about a successful connection to a server.
     */
    fun onConnected(client: GameClient) {
        Log.d("Network", "onConnected")
        notifyObservers { it.onConnected(client) }
    }

    /**
     * Notify all observers about the disconnect, then clear all observers, to never ever relay another message.
     */
    fun onDisconnected(client: GameClient, error: Exception?) {
        Log.d("Network", "onDisconnected")
        notifyObservers { it.onDisconnected(client, error) }

        synchronized(observer) {
            observer.clear()
        }
    }

    @WorkerThread
    @Throws(ProtocolException::class, GameStateException::class)
    override fun handleMessage(message: Message) {
        Log.d("Network", "<< $message")

        when(message) {
            is MessageGrantPlayer -> {
                assert(!game.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                game.setPlayerType(message.player, Game.PLAYER_LOCAL)
            }

            is MessageRevokePlayer -> {
                assert(!game.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                assert(game.isLocalPlayer(message.player)) { "revoked player ${message.player} is not local" }
                game.setPlayerType(message.player, Game.PLAYER_COMPUTER)
            }

            is MessageCurrentPlayer ->  {
                game.currentPlayer = message.player
                notifyObservers { it.newCurrentPlayer(message.player) }
            }

            is MessageSetStone -> {
                assert(game.isStarted || game.isFinished) { "received MSG_SET_STONE but game not yet running" }
                val turn = message.toTurn()
                assert(board.isValidTurn(turn)) { "invalid turn $turn" }

                game.history.add(turn)
                // inform listeners first, so that effects can be added before the stone
                // is committed. fixes drawing glitches, where stone is set, but
                // effect hasn't been added yet.

                notifyObservers { it.stoneWillBeSet(turn) }
                board.setStone(turn)
                notifyObservers { it.stoneHasBeenSet(turn) }
            }

            is MessageStoneHint -> {
                val turn = message.toTurn()
                notifyObservers { it.hintReceived(turn) }
            }

            is MessageGameFinish -> {
                assert(game.isStarted && !game.isFinished) { "Game in invalid state" }
                game.isFinished = true
                notifyObservers { it.gameFinished() }
            }

            is MessageServerStatus -> {
                // if game field size differs, start a new game with the new size
                assert(message.isAtLeastVersion(3)) { "Only version 3 or above supported" }

                if (!game.isStarted) {
                    board.startNewGame(message.gameMode, message.width, message.height)
                    board.setAvailableStones(message.stoneNumbers)
                }

                game.gameMode = message.gameMode

                when (game.gameMode) {
                    GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                    GameMode.GAMEMODE_DUO,
                    GameMode.GAMEMODE_JUNIOR -> {
                        var n = 0
                        while (n < Shape.COUNT) {
                            board.getPlayer(1).getStone(n).available = 0
                            board.getPlayer(3).getStone(n).available = 0
                            n++
                        }
                    }

                    else -> {}
                }

                notifyObservers { it.serverStatus(message) }

                // compare old and new status to find out who joined and who left
                lastStatus?.also { lastStatus ->
                    for (i in 0 until 4) {
                        val wasClient = lastStatus.clientForPlayer[i]
                        val isClient = message.clientForPlayer[i]
                        if (wasClient == null && isClient != null) { /* joined */
                            notifyObservers { it.playerJoined(i, isClient, message) }
                        } else if (wasClient != null && isClient == null) { /* left */
                            notifyObservers { it.playerLeft(i, wasClient, lastStatus) }
                        } else continue
                    }
                }

                lastStatus = message
            }

            is MessageChat -> {
                val status = lastStatus
                // we silently drop all chat messages if we don't have a last status, because we can't determine the player name
                if (message.client >= 0 && status != null) {
                    // ignore all server messages, because we create synthetic ones in server status

                    val player = status.clientForPlayer.indexOfFirst { it == message.client }

                    notifyObservers { it.chatReceived(status, message.client, player, message.message) }
                }
            }

            is MessageStartGame -> {
                assert(!game.isStarted) { "Game already started" }

                board.startNewGame(game.gameMode)
                game.isFinished = false
                game.isStarted = true
                /* Unbedingt history leeren. */
                game.history.clear()

                game.currentPlayer = -1

                notifyObservers { it.gameStarted() }
            }

            is MessageUndoStone -> {
                assert(game.isStarted || game.isFinished) { "received MSG_UNDO_STONE but game not running" }
                val turn: Turn = game.history.last
                board.undo(game.history, game.gameMode)

                notifyObservers { it.stoneUndone(turn) }
            }

            else -> throw ProtocolException("don't know how to handle message $message")
        }
    }

    @WorkerThread
    @Throws(ProtocolException::class, GameStateException::class)
    fun handleMessages(vararg message: Message) {
        message.forEach { handleMessage(it) }
    }

    @Throws(GameStateException::class)
    internal fun assert(condition: Boolean, lazyMessage: () -> String) {
        if (condition) return
        throw GameStateException(lazyMessage.invoke())
    }
}