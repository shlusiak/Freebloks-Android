package de.saschahlusiak.freebloks.client

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.network.message.*

/**
 * Processes network events and applies changes to the given [GameState] and notifies the [GameObserver]
 */
class NetworkEventHandler(private val game: GameState) {
    private val observer = mutableListOf<GameObserver>()

    fun addObserver(observer: GameObserver) {
        synchronized(observer) {
            this.observer.add(observer)
        }
    }

    fun removeObserver(observer: GameObserver) {
        synchronized(observer) {
            this.observer.remove(observer)
        }
    }

    private fun notifyObservers(block: (GameObserver) -> Unit) {
        synchronized(observer) {
            observer.forEach { block.invoke(it) }
        }
    }

    fun onConnected() {
        notifyObservers { it.onConnected(game) }
    }

    fun onDisconnected() {
        notifyObservers { it.onDisconnected(game) }
    }

    @WorkerThread
    @Throws(ProtocolException::class, GameStateException::class)
    fun handleMessage(message: Message) {
        when(message) {
            is MessageGrantPlayer -> {
                assert(!game.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                game.setPlayerType(message.player, GameState.PLAYER_LOCAL)
            }

            is MessageRevokePlayer -> {
                assert(!game.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                assert(game.isLocalPlayer(message.player)) { "revoked player ${message.player} is not local" }
                game.setPlayerType(message.player, GameState.PLAYER_COMPUTER)
            }

            is MessageCurrentPlayer ->  {
                game.currentPlayer = message.player
                notifyObservers { it.newCurrentPlayer(message.player) }
            }

            is MessageSetStone -> {
                assert(game.isStarted || game.isFinished) { "received MSG_SET_STONE but game not yet running" }
                val turn = message.toTurn()
                game.history.add(turn)
                // inform listeners first, so that effects can be added before the stone
                // is committed. fixes drawing glitches, where stone is set, but
                // effect hasn't been added yet.

                assert(game.isValidTurn(turn) != Spiel.FIELD_DENIED) { "game not in sync" }

                notifyObservers { it.stoneWillBeSet(turn) }
                game.setStone(turn)
                notifyObservers { it.stoneHasBeenSet(turn) }
            }

            is MessageStoneHint -> {
                val turn = message.toTurn()
                notifyObservers { it.hintReceived(turn) }
            }

            is MessageGameFinish -> {
                game.isFinished = true
                notifyObservers { it.gameFinished() }
            }

            is MessageServerStatus -> {
                // if game field size differs, start a new game with the new size
                if (!game.isStarted) {
                    game.startNewGame(message.gameMode, message.width, message.height)
                    if (message.isVersion(3)) game.setAvailableStones(message.stoneNumbers)
                }

                if (!message.isVersion(3)) {
                    throw GameStateException("Only version 3 supported")

                }
                game.gameMode = message.gameMode

                when (game.gameMode) {
                    GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                    GameMode.GAMEMODE_DUO,
                    GameMode.GAMEMODE_JUNIOR -> {
                        var n = 0
                        while (n < Shape.COUNT) {
                            game.getPlayer(1).getStone(n).available = 0
                            game.getPlayer(3).getStone(n).available = 0
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
                game.startNewGame(game.gameMode)
                game.isFinished = false
                game.isStarted = true
                /* Unbedingt history leeren. */
                game.history.clear()

                when (game.gameMode) {
                    GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                    GameMode.GAMEMODE_DUO,
                    GameMode.GAMEMODE_JUNIOR -> {
                        var n = 0
                        while (n < Shape.COUNT) {
                            game.getPlayer(1).getStone(n).available = 0
                            game.getPlayer(3).getStone(n).available = 0
                            n++
                        }
                    }
                }
                game.currentPlayer = -1
                game.refreshPlayerData()

                notifyObservers { it.gameStarted() }
            }

            is MessageUndoStone -> {
                if (!game.isStarted && !game.isFinished) throw GameStateException("received MSG_UNDO_STONE but game not running")
                val turn: Turn = game.history.last
                notifyObservers { it.stoneUndone(turn) }
                game.undo(game.history, game.gameMode)
            }

            else -> throw ProtocolException("don't know how to handle message $message")
        }
    }

    @Throws(GameStateException::class)
    internal fun assert(condition: Boolean, lazyMessage: () -> String) {
        if (condition) return
        throw GameStateException(lazyMessage.invoke())
    }
}