package de.saschahlusiak.freebloks.client

import android.util.Log
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageHandler
import de.saschahlusiak.freebloks.network.ProtocolException
import de.saschahlusiak.freebloks.network.message.*

/**
 * Processes incoming network events and applies changes to the given [Game] and notifies [GameEventObserver].
 */
class GameClientMessageHandler(private val game: Game): MessageHandler {
    private val tag = GameClientMessageHandler::class.java.simpleName

    private val observer = mutableListOf<GameEventObserver>()

    private val board = game.board

    fun addObserver(observer: GameEventObserver) {
        synchronized(observer) {
            this.observer.add(observer)
        }
    }

    fun removeObserver(observer: GameEventObserver) {
        synchronized(observer) {
            this.observer.remove(observer)
        }
    }

    private fun notifyObservers(block: (GameEventObserver) -> Unit) {
        synchronized(observer) {
            observer.forEach { block.invoke(it) }
        }
    }

    fun onConnected() {
        notifyObservers { it.onConnected(board) }
    }

    fun onDisconnected(error: Exception?) {
        notifyObservers { it.onDisconnected(board, error) }
    }

    @WorkerThread
    @Throws(ProtocolException::class, GameStateException::class)
    override fun handleMessage(message: Message) {
        Log.d(tag, message.toString())

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
            }

            is MessageChat -> {
                notifyObservers { it.chatReceived(message.client, message.message) }
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
                notifyObservers { it.stoneUndone(turn) }
                board.undo(game.history, game.gameMode)
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