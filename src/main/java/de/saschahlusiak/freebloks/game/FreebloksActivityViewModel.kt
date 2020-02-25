package de.saschahlusiak.freebloks.game

import android.app.Application
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.bluetooth.BluetoothClientToSocketThread
import de.saschahlusiak.freebloks.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.lobby.ChatEntry
import de.saschahlusiak.freebloks.lobby.ChatEntry.Companion.genericMessage
import de.saschahlusiak.freebloks.lobby.ChatEntry.Companion.serverMessage
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.view.scene.Intro
import de.saschahlusiak.freebloks.view.scene.Sounds
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.concurrent.thread

enum class ConnectionStatus {
    Disconnected, Connecting, Connected, Failed
}

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app), GameEventObserver {
    private val tag = FreebloksActivityViewModel::class.java.simpleName
    private val context = app
    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())

    // UI Thread handler
    private val handler = Handler()

    // services
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var notificationManager: MultiplayerNotificationManager? = null

    // settings
    private var vibrateOnMove: Boolean = false
    private var showNotifications: Boolean = true
    var soundsEnabled
        get() = sounds.isEnabled
        set(value) {
            prefs
                .edit()
                .putBoolean("sounds", value)
                .apply()
            sounds.isEnabled = value
            soundsEnabledLiveData.value = value
        }

    // other stuff
    var intro: Intro? = null
    private var connectThread: Thread? = null

    // client data
    var client: GameClient?
        private set
    val game get() = client?.game
    val board get() = client?.game?.board
    var lastStatus: MessageServerStatus? = null
        private set

    // if set, will display this player instead of the current player in the player sheet
    private var overrideShowPlayer: Int? = null

    val chatHistory = mutableListOf<ChatEntry>()
    val sounds = Sounds(app)

    // LiveData
    val chatHistoryAsLiveData = MutableLiveData(chatHistory)
    val soundsEnabledLiveData = MutableLiveData(sounds.isEnabled)
    val connectionStatusLiveData = MutableLiveData(ConnectionStatus.Disconnected)
    val playerToShowInSheet = MutableLiveData(-1)

    init {
        client = null
        reloadPreferences()
    }

    override fun onCleared() {
        disconnectClient()
        sounds.release()
    }

    fun reloadPreferences() {
        vibrateOnMove = prefs.getBoolean("vibrate", true)
        sounds.isEnabled = prefs.getBoolean("sounds", true)
        showNotifications = prefs.getBoolean("notifications", true)

        soundsEnabledLiveData.value = sounds.isEnabled

        if (showNotifications) {
            client?.let {
                if (notificationManager == null) {
                    notificationManager = MultiplayerNotificationManager(context, it)
                    it.addObserver(this)
                }
            }
        } else {
            notificationManager?.shutdown()
            notificationManager = null
        }
    }

    fun vibrate(milliseconds: Long) {
        @Suppress("DEPRECATION")
        if (vibrateOnMove)
            vibrator?.vibrate(milliseconds)
    }

    fun setClient(client: GameClient) {
        notificationManager?.shutdown()
        notificationManager = null

        this.client = client
        client.addObserver(this)

        if (showNotifications) {
            // registers itself to the game and listens for events
            notificationManager = MultiplayerNotificationManager(context, client).apply {
                client.addObserver(this)
            }
        }

        connectionStatusLiveData.postValue(ConnectionStatus.Disconnected)
    }

    fun onStart() {
        notificationManager?.stopBackgroundNotification()
    }

    fun onStop() {
        notificationManager?.startBackgroundNotification()
    }

    fun startConnectingClient(config: GameConfig, clientName: String?, requestStartGame: Boolean, onConnected: () -> Unit) {
        val client = client ?: return
        connectionStatusLiveData.postValue(ConnectionStatus.Connecting)
        setShowPlayerOverride(null)
        connectThread?.interrupt()

        connectThread = thread(name = "ConnectionThread") {
            val name = config.server ?: "(null)"
            Crashlytics.log(Log.INFO, tag, "Connecting to: $name")
            Crashlytics.setString("server", name)

            try {
                // client will notify observers about connection failed
                if (!client.connect(context, config.server, GameClient.DEFAULT_PORT)) {
                    // connection has failed, observers have been notified
                    connectionStatusLiveData.postValue(ConnectionStatus.Failed)
                    connectThread = null
                    return@thread

                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                // cancelled, ignore
                client.disconnect()
                connectionStatusLiveData.postValue(ConnectionStatus.Disconnected)
                connectThread = null
                return@thread
            }

            if (config.showLobby && config.server == null) {
                appendServerInterfacesToChat()
            }

            if (config.requestPlayers == null) {
                client.requestPlayer(-1, clientName)
            } else {
                for (i in 0..3)
                    if (config.requestPlayers[i])
                        client.requestPlayer(i, clientName)
            }
            if (config.showLobby) {
                if (config.server == null) {
                    // hosting a game locally, start bluetooth server and bridges.
                    // start a new client bridge for every connected bluetooth client
                    val connectedListener = object: OnBluetoothConnectedListener {
                        override fun onBluetoothClientConnected(socket: BluetoothSocket) {
                            BluetoothClientToSocketThread(socket, "localhost", GameClient.DEFAULT_PORT).start()
                        }
                    }
                    // the bluetooth server has to be constructed on the main thread
                    val bluetoothServer = BluetoothServerThread(connectedListener)
                    // the server is a thread and will have a strong reference for as long as it lives
                    client.addObserver(bluetoothServer)
                    bluetoothServer.start()
                }
            }

            if (requestStartGame) {
                client.requestGameStart();
            }

            connectionStatusLiveData.postValue(ConnectionStatus.Connected)
            handler.post { onConnected.invoke() }

            connectThread = null
        }
    }

    fun startConnectingClient(config: GameConfig, clientName: String?, requestStartGame: Boolean, onConnected: Runnable?) {
        startConnectingClient(config, clientName, requestStartGame) { onConnected?.run() }
    }

    fun disconnectClient() {
        connectThread?.interrupt()
        connectThread = null
        client?.disconnect()
        client = null
    }

    fun appendServerInterfacesToChat() {
        try {
            for (i in NetworkInterface.getNetworkInterfaces()) {
                for (address in i.inetAddresses) {
                    if (address.isAnyLocalAddress) continue
                    if (address.isLinkLocalAddress) continue
                    if (address.isLoopbackAddress) continue
                    if (address.isMulticastAddress) continue
                    var a = address.hostAddress
                    if (a.contains("%")) a = a.substring(0, a.indexOf("%"))

                    val e = genericMessage(String.format("[%s]", a))
                    chatHistory.add(e)
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        chatHistoryAsLiveData.postValue(chatHistory)
    }

    /**
     * Set the override of the player to show, via rotating of the board
     *
     * @param player the new player to show, or null for default
     */
    fun setShowPlayerOverride(player: Int?) {
        if (player == this.overrideShowPlayer) return

        this.overrideShowPlayer = player

        if (player != null) {
            playerToShowInSheet.postValue(player)
        } else {
            playerToShowInSheet.postValue(game?.currentPlayer ?: -1)
        }
    }

    //region GameEventObserver callbacks

    override fun onConnected(client: GameClient) {
        lastStatus = null
        connectionStatusLiveData.postValue(ConnectionStatus.Connected)
        overrideShowPlayer = null
        playerToShowInSheet.postValue(client.game.currentPlayer)
    }

    override fun serverStatus(status: MessageServerStatus) {
        this.lastStatus = status
    }

    override fun newCurrentPlayer(player: Int) {
        playerToShowInSheet.postValue(overrideShowPlayer ?: player)
    }

    override fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {
        val name = status.getClientName(client) ?: context.getString(R.string.client_d, client + 1)
        val e = ChatEntry.clientMessage(client, player, message, name)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory)
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {
        val clientName = name ?: context.getString(R.string.client_d, client + 1)

        // the names of colors
        val colorNames = context.resources.getStringArray(R.array.color_names)
        // the index into colorNames
        val playerColor = Global.getPlayerColor(player, game?.gameMode
            ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS)
        // the name of the player's color
        val colorName = colorNames[playerColor]

        val text = context.getString(R.string.player_joined_color, clientName, colorName)
        val e = serverMessage(player, text, clientName)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory)
    }

    override fun playerLeft(client: Int, player: Int, name: String?) {
        val clientName = name ?: context.getString(R.string.client_d, client + 1)

        // the names of colors
        val colorNames = context.resources.getStringArray(R.array.color_names)
        // the index into colorNames
        val playerColor = Global.getPlayerColor(player, game?.gameMode
            ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS)
        // the name of the player's color
        val colorName = colorNames[playerColor]

        val text = context.getString(R.string.player_left_color, clientName, colorName)
        val e = serverMessage(player, text, clientName)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory)
    }

    override fun onDisconnected(client: GameClient, error: Exception?) {
        lastStatus = null
        connectionStatusLiveData.postValue(ConnectionStatus.Disconnected)
        overrideShowPlayer = null
        playerToShowInSheet.postValue(-1)
        chatHistory.clear()
    }

    //endregion
}