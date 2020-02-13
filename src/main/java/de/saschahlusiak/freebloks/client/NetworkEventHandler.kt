package de.saschahlusiak.freebloks.client

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.network.message.*

/**
 * Processes network events and applies changes to the given [Game] and notifies the [GameEventObserver]
 */
class NetworkEventHandler(private val game: Game) {
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
    fun handleMessage(message: Message) {
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
                game.history.add(turn)
                // inform listeners first, so that effects can be added before the stone
                // is committed. fixes drawing glitches, where stone is set, but
                // effect hasn't been added yet.

                assert(board.isValidTurn(turn)) { "game not in sync" }

                notifyObservers { it.stoneWillBeSet(turn) }
                board.setStone(turn)
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
                    board.startNewGame(message.gameMode, message.width, message.height)
                    if (message.isVersion(3)) board.setAvailableStones(message.stoneNumbers)
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
                board.startNewGame(game.gameMode)
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
                            board.getPlayer(1).getStone(n).available = 0
                            board.getPlayer(3).getStone(n).available = 0
                            n++
                        }
                    }

                    GameMode.GAMEMODE_4_COLORS_2_PLAYERS,
                    GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> {}
                }
                board.refreshPlayerData()
                game.currentPlayer = -1

                notifyObservers { it.gameStarted() }
            }

            is MessageUndoStone -> {
                if (!game.isStarted && !game.isFinished) throw GameStateException("received MSG_UNDO_STONE but game not running")
                val turn: Turn = game.history.last
                notifyObservers { it.stoneUndone(turn) }
                board.undo(game.history, game.gameMode)
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