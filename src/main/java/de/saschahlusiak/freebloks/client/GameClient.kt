package de.saschahlusiak.freebloks.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.crashReporter
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageReadThread
import de.saschahlusiak.freebloks.network.MessageWriter
import de.saschahlusiak.freebloks.network.message.*
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
class GameClient @UiThread constructor(game: Game?, val config: GameConfig): Object() {
    private var clientSocket: Closeable?
    private var messageWriter: MessageWriter? = null
    private var readThread: MessageReadThread? = null
    private var sendExecutor: ExecutorService? = null
    private val gameClientMessageHandler: GameClientMessageHandler

    /**
     * The last status message received by our handler
     */
    val lastStatus get() = gameClientMessageHandler.lastStatus

    @JvmField
	val game: Game

    init {
        if (game == null) {
            val board = Board(config.fieldSize)
            board.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS)
            this.game = Game(board)
        } else this.game = game

        clientSocket = null
        gameClientMessageHandler = GameClientMessageHandler(this.game)
    }

    @Throws(Throwable::class)
    override fun finalize() {
        disconnect()
        super.finalize()
    }

    fun isConnected(): Boolean {
        return when (val socket = clientSocket) {
            null -> false

            is Socket -> !socket.isClosed
            is BluetoothSocket -> socket.isConnected

            else -> true
        }
    }

    fun addObserver(sci: GameEventObserver) = gameClientMessageHandler.addObserver(sci)
    fun removeObserver(sci: GameEventObserver) = gameClientMessageHandler.removeObserver(sci)

    /**
     * Try to establish a TCP connection to the given host and port.
     * On success will call [.connected]
     *
     * @param context for getString()
     * @param host target hostname or null for localhost
     * @param port port
     *
     * @throws IOException on connection refused
     */
    @WorkerThread
    fun connect(context: Context, host: String?, port: Int): Boolean {
        val socket = Socket()
        val address = host?.let { InetSocketAddress(it, port) } ?: InetSocketAddress(null as InetAddress?, port)

        try {
            socket.connect(address, CONNECT_TIMEOUT)
        } catch (e: IOException) {
            if (Thread.interrupted()) return false

            e.printStackTrace()

            // translate any IOException to "Connection refused"
            val e2 = IOException(context.getString(R.string.connection_refused), e)
            gameClientMessageHandler.notifyConnectionFailed(this, e2)

            return false
        }
        connected(socket, socket.getInputStream(), socket.getOutputStream())
        return true
    }

    /**
     * Try to connect to the given remote bluetooth device
     *
     * On success will call [.connected]
     */
    @WorkerThread
    fun connect(context: Context, remote: BluetoothDevice): Boolean {
        val socket: BluetoothSocket
        try {
            socket = remote.createInsecureRfcommSocketToServiceRecord(BluetoothServerThread.SERVICE_UUID)
            socket.connect()
        }  catch (e: IOException) {
            if (Thread.interrupted()) return false

            e.printStackTrace()

            // translate any IOException to "Connection refused"
            val e2 = IOException(context.getString(R.string.connection_refused), e)
            gameClientMessageHandler.notifyConnectionFailed(this, e2)

            return false
        }

        connected(socket, socket.inputStream, socket.outputStream)
        return true
    }

    /**
     * Connection is successful, set up message readers and writers.
     * Make sure you have observers registered before calling this method.
     *
     * @param socket a closeable socket, for disconnecting
     * @param input the InputStream from the socket
     * @param output the OutputStream to the socket
     */
    @Synchronized
    fun connected(socket: Closeable, input: InputStream, output: OutputStream) {
        clientSocket = socket

        // first we set up writing to the server
        messageWriter = MessageWriter(output)
        sendExecutor = Executors.newSingleThreadExecutor()

        // we start reading from the server, we will likely begin receiving and processing
        // messages and sending events to the observers.
        // To allow for other observers to be registered before we consume messages, we inform the
        // observers so far about it.
        gameClientMessageHandler.notifyConnected(this)

        readThread = MessageReadThread(input, gameClientMessageHandler) { 
            disconnect()
        }.also { it.start() }
    }

    @Synchronized
    fun disconnect() {
        val socket = clientSocket ?: return

        val lastError = readThread?.error
        try {
            crashReporter.log("Disconnecting from ${config.server}")

            readThread?.goDown()
            if (sendExecutor != null) {
                sendExecutor?.shutdown()
                try {
                    sendExecutor?.awaitTermination(200, TimeUnit.MILLISECONDS)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            if (socket is Socket && socket.isConnected) {
                socket.shutdownInput()
            }
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        readThread = null
        sendExecutor = null
        clientSocket = null

        gameClientMessageHandler.notifyDisconnected(this, lastError)
    }

    /**
     * Request a new player from the server
     * @param player player to request or -1 for random
     * @param name name for the player
     */
    fun requestPlayer(player: Int, name: String?) {
        // be aware that the name may be capped at length 16
        send(MessageRequestPlayer(player, name))
    }

    /**
     * Request to revoke the given player
     * @param player the local player to revoke
     */
    fun revokePlayer(player: Int) {
        if (!game.isLocalPlayer(player)) return
        send(MessageRevokePlayer(player))
    }

    /**
     * Request a new game mode with new board sizes from the server.
     *
     * @param width new width to request
     * @param height new height to request
     * @param gameMode new game mode to request
     * @param stones availability of the 21 stones
     */
    fun requestGameMode(width: Int, height: Int, gameMode: GameMode, stones: IntArray) {
        send(MessageRequestGameMode(width, height, gameMode, stones))
    }

    /**
     * Request a hint for the current local player.
     */
    fun requestHint() {
        if (!isConnected()) return
        if (!game.isLocalPlayer()) return
        send(MessageRequestHint(game.currentPlayer))
    }

    /**
     * Send a chat message to the server, which will relay it back
     * @param message the message
     */
    fun sendChat(message: String) { // the client does not matter, it will be filled in by the server then broadcasted to all clients
        send(MessageChat(0, message))
    }

    /**
     * Called by the UI for the local player to place the stone.
     * The request is sent to the server, the stone will not be placed locally.
     */
    fun setStone(turn: Turn) {
// locally set no player as the current player
// on success the server will send us the new current player
        game.currentPlayer = -1
        send(MessageSetStone(turn))
    }

    /**
     * Request server to start the game
     */
    fun requestGameStart() {
        send(MessageStartGame())
    }

    /**
     * Request server to undo the last move
     */
    fun requestUndo() {
        if (!isConnected()) return
        if (!game.isLocalPlayer()) return
        send(MessageRequestUndo())
    }

    /**
     * Relay the given message to the sendExecutor to be sent to the server asynchronously.
     * Write errors will be silently ignored.
     *
     * @param msg the message to send
     */
    @AnyThread
    private fun send(msg: Message) {
        // ignore sending to closed stream
        try {
            sendExecutor?.submit { messageWriter?.write(msg) }
        } catch (e: RejectedExecutionException) {
            crashReporter.logException(e)
        }
    }

    companion object {
        private const val CONNECT_TIMEOUT = 5000
        const val DEFAULT_PORT = 59995
    }
}