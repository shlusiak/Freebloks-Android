package de.saschahlusiak.freebloks.controller

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.Spiel
import de.saschahlusiak.freebloks.model.Spielleiter
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.network.message.*

/**
 * Processes network events and applies changes to the given [Spielleiter] and notifies the [GameObserver]
 */
class NetworkEventHandler(private val spiel: Spielleiter) {
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
        notifyObservers { it.onConnected(spiel) }
    }

    fun onDisconnected() {
        notifyObservers { it.onDisconnected(spiel) }
    }

    @WorkerThread
    @Throws(ProtocolException::class, GameStateException::class)
    fun handleMessage(message: Message) {
        when(message) {
            is MessageGrantPlayer -> {
                assert(!spiel.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                spiel.spieler[message.player] = Spielleiter.PLAYER_LOCAL
            }

            is MessageRevokePlayer -> {
                assert(!spiel.isStarted) { "received MSG_REVOKE_PLAYER but game is running" }
                assert(spiel.spieler[message.player] == Spielleiter.PLAYER_LOCAL) {
                    "revoked player ${message.player} is not local"
                }
                spiel.spieler[message.player] = Spielleiter.PLAYER_COMPUTER
            }

            is MessageCurrentPlayer ->  {
                spiel.m_current_player = message.player
                notifyObservers { it.newCurrentPlayer(message.player) }
            }

            is MessageSetStone -> {
                assert(spiel.isStarted || spiel.isFinished) { "received MSG_SET_STONE but game not yet running" }
                val turn = message.toTurn()
                spiel.addHistory(turn)
                // inform listeners first, so that effects can be added before the stone
                // is committed. fixes drawing glitches, where stone is set, but
                // effect hasn't been added yet.

                assert(spiel.isValidTurn(turn) != Spiel.FIELD_DENIED) { "game not in sync" }

                notifyObservers { it.stoneWillBeSet(turn) }
                spiel.setStone(turn)
                notifyObservers { it.stoneHasBeenSet(turn) }
            }

            is MessageStoneHint -> {
                val turn = message.toTurn()
                notifyObservers { it.hintReceived(turn) }
            }

            is MessageGameFinish -> {
                spiel.isFinished = true
                notifyObservers { it.gameFinished() }
            }

            is MessageServerStatus -> {
                // if game field size differs, start a new game with the new size
                if (!spiel.isStarted) {
                    spiel.startNewGame(message.gameMode, message.width, message.height)
                    if (message.isVersion(3)) spiel.setAvailableStones(message.stoneNumbers)
                }

                if (!message.isVersion(3)) {
                    throw GameStateException("Only version 3 supported")

                }
                spiel.setGameMode(message.gameMode)

                when (spiel.gameMode) {
                    GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                    GameMode.GAMEMODE_DUO,
                    GameMode.GAMEMODE_JUNIOR -> {
                        var n = 0
                        while (n < Shape.COUNT) {
                            spiel.getPlayer(1).getStone(n).available = 0
                            spiel.getPlayer(3).getStone(n).available = 0
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
                spiel.startNewGame(spiel.getGameMode())
                spiel.setFinished(false)
                spiel.setStarted(true)
                /* Unbedingt history leeren. */
                if (spiel.history != null) spiel.history.clear()
                //			setAvailableStones(status.stone_numbers[0],status.stone_numbers[1],status.stone_numbers[2],status.stone_numbers[3],status.stone_numbers[4]);
                if (spiel.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS || spiel.getGameMode() == GameMode.GAMEMODE_DUO || spiel.getGameMode() == GameMode.GAMEMODE_JUNIOR) {
                    var n = 0
                    while (n < Shape.COUNT) {
                        spiel.getPlayer(1).getStone(n).available = 0
                        spiel.getPlayer(3).getStone(n).available = 0
                        n++
                    }
                }
                spiel.m_current_player = -1
                spiel.refreshPlayerData()

                notifyObservers { it.gameStarted() }
            }

            is MessageUndoStone -> {
                if (!spiel.isStarted && !spiel.isFinished) throw GameStateException("received MSG_UNDO_STONE but game not running")
                val turn: Turn = spiel.history.last
                notifyObservers { it.stoneUndone(turn) }
                spiel.undo(spiel.history, spiel.getGameMode())
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