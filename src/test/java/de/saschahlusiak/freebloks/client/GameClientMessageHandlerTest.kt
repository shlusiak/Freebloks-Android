package de.saschahlusiak.freebloks.client

import de.saschahlusiak.freebloks.game.GameConfiguration
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.model.Board.Companion.FIELD_FREE
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_4_PLAYERS
import de.saschahlusiak.freebloks.network.ProtocolException
import de.saschahlusiak.freebloks.network.message.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class GameClientMessageHandlerTest {
    private lateinit var game: Game
    private lateinit var handler: GameClientMessageHandler

    private fun serverStatus(size: Int = Board.DEFAULT_BOARD_SIZE, gameMode: GameMode = GAMEMODE_4_COLORS_4_PLAYERS): MessageServerStatus {
        return MessageServerStatus(
            0, 4, 0,
            size, size,
            gameMode,
            arrayOfNulls(4),
            arrayOfNulls(8),
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
            override fun chatReceived(client: Int, message: String) {
                assertNull(receivedMessage)
                receivedMessage = Pair(client, message)
            }
        }

        handler.addObserver(observer)

        val msg = MessageChat(-1, "hello")
        handler.handleMessage(msg)

        // test removeObserver by removing us and sending another message
        handler.removeObserver(observer)
        handler.handleMessage(MessageChat(1, "other"))

        assertNotNull(receivedMessage)
        val (client, message) = receivedMessage ?: throw java.lang.IllegalStateException("message not received")
        assertEquals(-1, client)
        assertEquals("hello", message)
    }

    @Test
    fun test_onConnected() {
        var connected = false
        val observer = object : GameEventObserver {
            override fun onConnected(client: GameClient) {
                connected = true
            }
        }

        handler.addObserver(observer)

        val client = GameClient(Game(), GameConfiguration.Builder().build())
        handler.onConnected(client)
        assertTrue(connected)
    }

    @Test
    fun test_onDisconnected() {
        var disconnected = false
        var receivedError: Exception? = null

        val observer = object : GameEventObserver {
            override fun onDisconnected(client: GameClient, error: Exception?) {
                disconnected = true
                receivedError = error
            }
        }

        handler.addObserver(observer)

        val client = GameClient(Game(), GameConfiguration.Builder().build())

        handler.onDisconnected(client, IOException("stuff"))
        assertTrue(disconnected)
        assertTrue(receivedError is IOException)
    }

    @Test(expected = ProtocolException::class)
    fun test_notHandledMessage() {
        handler.handleMessage(MessageRequestHint(1))
    }
}