package de.saschahlusiak.freebloks.client

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.model.Game.Companion.PLAYER_LOCAL
import de.saschahlusiak.freebloks.network.*
import de.saschahlusiak.freebloks.network.message.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.Closeable
import java.io.EOFException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GameClientTest {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val game = Game()
    private val client = GameClient(game, GameConfig())

    private val toServerStream = PipedOutputStream()
    private val fromServerStream = PipedInputStream()
    private val toClientStream = PipedOutputStream()
    private val fromClientStream = PipedInputStream()

    private val toClient = MessageWriter(toClientStream)

    private lateinit var server: Job
    private var readerClosed = false
    private var serverDisconnectError: Throwable? = null
    private var clientDisconnectError: Throwable? = null
    private val messages = Channel<Message>(capacity = Channel.UNLIMITED)

    /**
     * Simulate a server and collect messages received from the client
     */
    private val serverMessageHandler = object : MessageHandler, Closeable {
        override fun handleMessage(message: Message) {
            messages.sendBlocking(message)
        }

        override fun close() {
            toServerStream.close()
            fromServerStream.close()
            readerClosed = true
            messages.close()
        }
    }

    private enum class Event {
        Status, Disconnected
    }

    /**
     * Used to watch for the client to have processed a server status message
     */
    private val gameObserver = object : GameEventObserver {

        private val events = Channel<Event>(Channel.UNLIMITED)
        var statusReceived: Boolean = false

        @WorkerThread
        override fun serverStatus(status: MessageServerStatus) {
            events.sendBlocking(Event.Status)
        }

        @UiThread
        override fun onDisconnected(client: GameClient, error: Throwable?) {
            clientDisconnectError = error
            events.sendBlocking(Event.Disconnected)
        }

        /**
         * Block until the server status message is received
         */
        suspend fun awaitStatus() {
            for (event in events) {
                if (event == Event.Status) return
            }
        }

        /**
         * Block until the client is disconnected
         */
        suspend fun awaitClientDisconnect() {
            if (!client.isConnected()) return
            for (event in events) {
                if (event == Event.Disconnected) {
                    assertFalse(client.isConnected())
                    return
                }
            }
        }
    }

    private suspend fun awaitClientDisconnect() = gameObserver.awaitClientDisconnect()

    /**
     * Sends out a [MessageServerStatus] to the client and blocks until it is processed
     */
    private fun MessageWriter.flush() = runBlocking {
        gameObserver.statusReceived = false

        write(
            MessageServerStatus(
                0, 4, 1,
                game.board.width, game.board.height,
                game.gameMode,
                arrayOfNulls(4),
                arrayOfNulls(8)
            )
        )

        gameObserver.awaitStatus()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Default)

        fromServerStream.connect(toClientStream)
        fromClientStream.connect(toServerStream)

        client.connected(serverMessageHandler, fromServerStream, toServerStream)
        client.addObserver(gameObserver)

        server = MessageReader(fromClientStream).asFlow()
            .onEach { serverMessageHandler.handleMessage(it) }
            .catch { serverDisconnectError = it }
            .launchIn(scope)

        // player 1 is local and the current player
        toClient.write(
            MessageGrantPlayer(1),
            MessageCurrentPlayer(1)
        )

        toClient.flush()

        assertEquals(1, game.currentPlayer)
        assertTrue(game.isLocalPlayer(1))
    }

    @After
    fun teardown() {
        client.disconnect()
        scope.cancel()
    }

    @Test
    fun test_gameClient_notConnected() {
        val client = GameClient(Game(), GameConfig())
        assertFalse(client.isConnected())
    }

    @Test
    fun test_gameClient_connected() {
        assertTrue(client.isConnected())
        assertNull(clientDisconnectError)
    }

    @Test
    fun test_gameClient_client_disconnect() = runBlocking {
        client.disconnect()
        server.join()
        assertFalse(client.isConnected())
        assertTrue(readerClosed)
        assertNull(clientDisconnectError)
        assertTrue(serverDisconnectError is EOFException)
        assertTrue(messages.toList().isEmpty())
    }

    @Test
    fun test_gameClient_server_disconnect() = runBlocking {
        assertTrue(client.isConnected())
        toClientStream.close()
        awaitClientDisconnect()

        assertFalse(client.isConnected())
        assertTrue(clientDisconnectError is EOFException)
    }

    @Test
    fun test_gameClient_requestPlayer() = runBlocking {
        client.requestPlayer(3, "hello")

        assertEquals(MessageRequestPlayer(3, "hello"), messages.receive())
    }

    @Test
    fun test_gameClient_requestHint() = runBlocking {
        // client will refuse to send request if the current player is not local
        client.game.currentPlayer = 2
        client.game.setPlayerType(2, PLAYER_LOCAL)

        client.requestHint()

        assertEquals(MessageRequestHint(2), messages.receive())
    }

    @Test
    fun test_gameClient_requestStart() = runBlocking {
        client.requestGameStart()

        assertEquals(MessageStartGame(), messages.receive())
    }

    @Test
    fun test_gameClient_revokePlayer() = runBlocking {
        client.revokePlayer(1)

        assertEquals(MessageRevokePlayer(1), messages.receive())
    }

    @Test
    fun test_gameClient_chat() = runBlocking {
        client.sendChat("Hey hey")

        assertEquals(MessageChat(0, "Hey hey"), messages.receive())
    }

    @Test
    fun test_gameClient_setStone() = runBlocking {
        client.setStone(Turn(1, 2, 3, 4, Orientation.Default))
        assertEquals(-1, client.game.currentPlayer)

        assertEquals(MessageSetStone(1, 2, Orientation.Default, 4, 3), messages.receive())
    }

    @Test
    fun test_gameClient_undo() = runBlocking {
        client.requestUndo()

        assertEquals(MessageRequestUndo(), messages.receive())
    }

    @Test
    fun test_gameClient_requestGameMode() = runBlocking {
        client.requestGameMode(17, 17, GameMode.GAMEMODE_DUO, IntArray(21) { 1 })

        assertEquals(MessageRequestGameMode(17, 17, GameMode.GAMEMODE_DUO, IntArray(21) { 1 }), messages.receive())
    }

    @Test
    fun test_gameClient_readInvalidHeader() = runBlocking {
        val dataInvalidHeader = ubyteArrayOf(
            //region data
            0x07, 0x00, 0x06, 0x03, 0xd5, 0x03
            //endregion
        )

        toClientStream.write(dataInvalidHeader)
        toClientStream.close()

        // Client will disconnect on its own on exception, so we can still join on the serverThread
        gameObserver.awaitClientDisconnect()

        assertTrue(clientDisconnectError is ProtocolException)
    }

    @Test
    fun test_gameClient_readDataIncomplete() = runBlocking {
        val dataIncomplete = ubyteArrayOf(
            //region data
            0x06, 0x00, 0xab, 0x07, 0xd7, 0x00, 0x04, 0x01, 0x14, 0x14, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 0xfe, 0xfe, 0xfe, 0xfe, 0x00, 0xae, 0x62, 0xc4, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xb8, 0xab, 0x50, 0xbb, 0x00, 0x0b, 0x15, 0xee, 0x04, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x40, 0xc4, 0xf4, 0xac,
            0x85, 0xd8, 0x00, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x00, 0x00, 0x40, 0x91, 0x22, 0xee, 0x01, 0x00, 0x00, 0x00, 0x00, 0xac, 0x50, 0xbb, 0x38, 0xac, 0x50,
            0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xab, 0xa3, 0xa0, 0xa7, 0x00, 0x91, 0x22, 0xee, 0x46, 0xac, 0x50, 0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xe8, 0xab, 0x50, 0xbb,
            0x00, 0xae, 0x62, 0xc4, 0x18, 0x00, 0x00, 0x00, 0x34, 0x7c, 0x22, 0xee, 0xe8, 0xab, 0x50, 0xbb, 0x00, 0x83, 0x14, 0xee, 0xa0, 0xae, 0x62, 0xc4, 0x08,
            0x02, 0xff, 0xff, 0xf8, 0xab, 0x50, 0xbb, 0x00, 0x84, 0x14, 0xee, 0xff, 0x25, 0x19, 0xee, 0xc4, 0xfd, 0x51, 0xbb, 0x28, 0xac, 0x50, 0xbb, 0x03, 0x01,
            0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x06, 0x00, 0xab, 0x07,
            0xd7, 0x00, 0x04, 0x01, 0x14, 0x14, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 0xfe, 0xfe, 0xfe, 0xfe, 0x00, 0x65, 0x51, 0xbb, 0xf8, 0x16, 0x89, 0xd8, 0x04,
            0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x65, 0x51, 0xbb, 0x14, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00
            //endregion
        )

        toClientStream.write(dataIncomplete)
        toClientStream.close()

        // Client will disconnect on its own on exception, so we can still join on the serverThread
        gameObserver.awaitClientDisconnect()

        assertTrue(clientDisconnectError is EOFException)
    }

    @Test
    fun test_gameClient_readDataInvalidGameState() = runBlocking {
        val dataUndoStone = ubyteArrayOf(
            //region data
            0x0b, 0x00, 0x0b, 0x0a, 0xe7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
            //endregion
        )

        toClientStream.write(dataUndoStone)
        toClientStream.close()

        gameObserver.awaitClientDisconnect()

        assertTrue(clientDisconnectError is GameStateException)
    }
}