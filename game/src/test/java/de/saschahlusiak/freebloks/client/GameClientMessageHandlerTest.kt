package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.model.Board.Companion.FIELD_FREE
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_4_PLAYERS
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.ProtocolException
import de.saschahlusiak.freebloks.network.message.*
import de.saschahlusiak.freebloks.utils.CrashReporter
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GameClientMessageHandlerTest {
    private lateinit var game: Game
    private lateinit var handler: GameClientMessageHandler

    private fun GameClientMessageHandler.handleMessages(vararg message: Message) {
        message.forEach { handleMessage(it) }
    }

    private fun serverStatus(
        size: Int = Board.DEFAULT_BOARD_SIZE,
        gameMode: GameMode = GAMEMODE_4_COLORS_4_PLAYERS,
        clientForPlayer: Array<Int?> = arrayOfNulls(4),
        clientNames: Array<String?> = arrayOfNulls(8)
    ): MessageServerStatus {
        return MessageServerStatus(
            0, 4, 0,
            size, size,
            gameMode,
            clientForPlayer,
            clientNames,
            stoneNumbers = IntArray(21) { 1 }
        )
    }

    @Before
    fun setup() {
        game = Game()
        handler = GameClientMessageHandler(game)
        // send default server status
        handler.handleMessage(serverStatus())

        assertFalse(game.isStarted)
        assertFalse(game.isFinished)
        assertEquals(GAMEMODE_4_COLORS_4_PLAYERS, game.gameMode)
        assertEquals(20, game.board.width)
        assertEquals(20, game.board.height)
        assertEquals(-1, game.currentPlayer)
    }

    @Test
    fun test_startNewGame() {
        handler.handleMessage(MessageStartGame())
        assertTrue(game.isStarted)
        assertEquals(-1, game.currentPlayer)
    }

    @Test(expected = GameStateException::class)
    fun test_startNewGame_started() {
        handler.handleMessage(MessageStartGame())
        handler.handleMessage(MessageStartGame())
    }

    @Test(expected = GameStateException::class)
    fun test_finishGame_notStarted() {
        handler.handleMessage(MessageGameFinish())
    }

    @Test
    fun test_finishGame() {
        handler.handleMessages(
            MessageStartGame(),
            MessageGameFinish()
        )
        assertTrue(game.isStarted)
        assertTrue(game.isFinished)
    }

    @Test
    fun test_grantPlayer_revokePlayer() {
        assertFalse(game.isLocalPlayer(2))
        handler.handleMessage(MessageGrantPlayer(2))
        assertTrue(game.isLocalPlayer(2))
        handler.handleMessage(MessageRevokePlayer(2))
        assertFalse(game.isLocalPlayer(2))
    }

    @Test(expected = GameStateException::class)
    fun test_grantPlayer_started() {
        assertFalse(game.isLocalPlayer(2))
        handler.handleMessage(MessageStartGame())
        handler.handleMessage(MessageGrantPlayer(2))
    }

    @Test(expected = GameStateException::class)
    fun test_revokePlayer_started() {
        assertFalse(game.isLocalPlayer(2))
        handler.handleMessage(MessageGrantPlayer(2))
        handler.handleMessage(MessageStartGame())
        handler.handleMessage(MessageRevokePlayer(2))
    }

    @Test(expected = GameStateException::class)
    fun test_setStone_notStarted() {
        handler.handleMessage(MessageSetStone(1, 2, Orientation.Default, 0, 0))
    }

    @Test
    fun test_setStone() {
        val seed = game.board.getPlayerSeed(0, game.gameMode) ?: throw IllegalStateException()
        assertEquals(FIELD_FREE, game.board.getFieldPlayer(seed))
        handler.handleMessages(
            serverStatus(),
            MessageStartGame(),
            MessageCurrentPlayer(0),
            MessageSetStone(0, 0, Orientation.Default, seed.x, seed.y)
        )
        assertEquals(0, game.currentPlayer)
        assertEquals(0, game.board.getFieldPlayer(seed))
    }

    @Test
    fun test_setStone_undoStone() {
        val seed = game.board.getPlayerSeed(0, game.gameMode) ?: throw IllegalStateException()
        assertEquals(FIELD_FREE, game.board.getFieldPlayer(seed))
        handler.handleMessages(
            serverStatus(),
            MessageStartGame(),
            MessageCurrentPlayer(0),
            MessageSetStone(0, 0, Orientation.Default, seed.x, seed.y),
            MessageUndoStone()
        )
        assertEquals(0, game.currentPlayer)
        assertEquals(FIELD_FREE, game.board.getFieldPlayer(seed))
    }

    @Test
    fun test_setStone_duo() {
        handler.handleMessages(
            serverStatus(15, GameMode.GAMEMODE_DUO),
            MessageStartGame()
        )

        assertEquals(15, game.board.width)
        assertEquals(15, game.board.height)
        val seed = game.board.getPlayerSeed(0, game.gameMode) ?: throw IllegalStateException()
        assertEquals(FIELD_FREE, game.board.getFieldPlayer(seed))

        handler.handleMessages(
            MessageCurrentPlayer(0),
            MessageSetStone(0, 0, Orientation.Default, seed.x, seed.y)
        )
        assertEquals(0, game.currentPlayer)
        assertEquals(0, game.board.getFieldPlayer(seed))
    }

    @Test(expected = GameStateException::class)
    fun test_setStone_invalidTurn() {
        handler.handleMessages(
            MessageStartGame(),
            MessageCurrentPlayer(0),
            MessageSetStone(0, 0, Orientation.Default, 4, 5)
        )
    }

    @Test
    fun test_hint() {
        var receivedHint: Turn? = null

        val observer = object : GameEventObserver {
            override fun hintReceived(turn: Turn) {
                assertNull(receivedHint)
                receivedHint = turn
            }
        }

        handler.addObserver(observer)

        val msg = MessageStoneHint(1, 2, false, Rotation.Right, 5, 6)
        handler.handleMessage(msg)

        assertNotNull(receivedHint)
        assertEquals(msg.toTurn(), receivedHint)
    }

    @Test
    fun test_chat() {
        var receivedMessage: Pair<Int, String>? = null
        val observer = object : GameEventObserver {
            override fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {
                assertNull(receivedMessage)
                receivedMessage = Pair(client, message)
            }
        }

        handler.addObserver(observer)

        // chat messages are ignored until we have received a server status
        handler.handleMessage(serverStatus())

        // server messages are ignored
        handler.handleMessage(MessageChat(-1, "hello"))

        // but this one comes through
        val msg = MessageChat(3, "hello")
        handler.handleMessage(msg)

        // test removeObserver by removing us and sending another message
        handler.removeObserver(observer)
        handler.handleMessage(MessageChat(1, "other"))

        assertNotNull(receivedMessage)
        val (client, message) = receivedMessage ?: throw java.lang.IllegalStateException("message not received")
        assertEquals(3, client)
        assertEquals("hello", message)
    }


    @Test
    fun test_onPlayer_joined_left() {
        data class Joined(val client: Int, val player: Int, val name: String?)
        data class Left(val client: Int, val player: Int, val name: String?)
        val events = mutableListOf<Any>()

        val observer = object : GameEventObserver {
            override fun playerJoined(client: Int, player: Int, name: String?) {
                events.add(Joined(client, player, name))
            }

            override fun playerLeft(client: Int, player: Int, name: String?) {
                events.add(Left(client, player, name))
            }
        }

        handler.addObserver(observer)

        handler.handleMessage(serverStatus(
            clientForPlayer = arrayOf(null, 2, null, null),
            clientNames = arrayOf(null, null, null, null, null, null, null, null)
        ))

        handler.handleMessage(serverStatus(
            clientForPlayer = arrayOf(null, 2, 3, null),
            clientNames = arrayOf(null, null, "Paul", "Peter", null, null, null, null)
        ))

        handler.handleMessage(serverStatus(
            clientForPlayer = arrayOf(null, 2, 3, 1),
            clientNames = arrayOf(null, null, "Paul", "Peter", null, null, null, null)
        ))

        handler.handleMessage(serverStatus(
            clientForPlayer = arrayOf(null, null, 3, 1),
            clientNames = arrayOf(null, null, null, "Peter", null, null, null, null)
        ))

        handler.handleMessages(serverStatus())

        assertEquals(listOf(
            Joined(2, 1, null),
            Joined(3, 2, "Peter"),
            Joined(1, 3, null),
            Left(2, 1, "Paul"),
            Left(3, 2, "Peter"),
            Left(1, 3, null)
        ), events)
    }

    @Test
    fun test_notifyConnected() {
        var connected = false
        val observer = object : GameEventObserver {
            override fun onConnected(client: GameClient) {
                connected = true
            }
        }

        handler.addObserver(observer)

        val client = GameClient(Game(), GameConfig(), CrashReporter())
        handler.notifyConnected(client)
        assertTrue(connected)
    }

    @Test
    fun test_notifyConnectionFailed() {
        var receivedError: Exception? = null
        val observer = object : GameEventObserver {
            override fun onConnectionFailed(client: GameClient, error: Exception) {
                receivedError = error
            }
        }

        handler.addObserver(observer)

        val client = GameClient(Game(), GameConfig(), CrashReporter())
        handler.notifyConnectionFailed(client, IOException("Stuff"))
        assertNotNull(receivedError)
        assertTrue(receivedError is IOException)
    }

    @Test
    fun test_notifyDisconnected() {
        var disconnected = false
        var receivedError: Throwable? = null

        val observer = object : GameEventObserver {
            override fun onDisconnected(client: GameClient, error: Throwable?) {
                disconnected = true
                receivedError = error
            }
        }

        handler.addObserver(observer)

        val client = GameClient(Game(), GameConfig(), CrashReporter())

        handler.notifyDisconnected(client, IOException("stuff"))
        assertTrue(disconnected)
        assertTrue(receivedError is IOException)
    }

    @Test(expected = ProtocolException::class)
    fun test_notHandledMessage() {
        handler.handleMessage(MessageRequestHint(1))
    }
}