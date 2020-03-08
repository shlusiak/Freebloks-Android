package de.saschahlusiak.freebloks.game

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Vibrator
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothClientToSocketThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.game.lobby.ChatEntry
import de.saschahlusiak.freebloks.game.lobby.ChatEntry.Companion.genericMessage
import de.saschahlusiak.freebloks.game.lobby.ChatEntry.Companion.serverMessage
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import de.saschahlusiak.freebloks.view.scene.Intro
import de.saschahlusiak.freebloks.theme.Sounds
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.concurrent.thread

enum class ConnectionStatus {
    Disconnected, Connecting, Connected, Failed
}

data class SheetPlayer(
    // the player to show on the board or -1
    val player: Int,
    // whether the board is currently rotated or in its "home" position.
    val isRotated: Boolean
)

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app), GameEventObserver, GooglePlayGamesHelper.GameHelperListener {
    private val tag = FreebloksActivityViewModel::class.java.simpleName
    private val context = app
    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
    private val analytics by lazy { FirebaseAnalytics.getInstance(context) }

    // UI Thread handler
    private val handler = Handler()

    // services
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var notificationManager: MultiplayerNotificationManager? = null

    // settings
    private var vibrateOnMove: Boolean = false
    private var showNotifications: Boolean = true
    val soundsEnabled get() = sounds.isEnabled
    // TODO: I think we should ditch this override completely and only support what was set during game start. What do you think?
    var localClientNameOverride: String? = null
        private set

    // other stuff
    var intro: Intro? = null
    private var connectThread: Thread? = null
    val gameHelper = GooglePlayGamesHelper(app, this)

    // client data
    var client: GameClient?
        private set
    val game get() = client?.game
    val board get() = client?.game?.board
    var lastStatus: MessageServerStatus? = null
        private set

    private val chatHistory = mutableListOf<ChatEntry>()
    val sounds = Sounds(app)

    // LiveData
    val chatHistoryAsLiveData = MutableLiveData(chatHistory)
    val soundsEnabledLiveData = MutableLiveData(sounds.isEnabled)
    val connectionStatus = MutableLiveData(ConnectionStatus.Disconnected)
    val playerToShowInSheet = MutableLiveData(SheetPlayer(-1, false))
    val googleAccountSignedIn = MutableLiveData(false)
    val canRequestUndo = MutableLiveData(false)
    val canRequestHint = MutableLiveData(false)

    init {
        client = null
        reloadPreferences()
    }

    override fun onCleared() {
        disconnectClient()
        sounds.release()
    }

    @UiThread
    fun reloadPreferences() {
        vibrateOnMove = prefs.getBoolean("vibrate", true)
        sounds.isEnabled = prefs.getBoolean("sounds", true)
        showNotifications = prefs.getBoolean("notifications", true)
        localClientNameOverride = prefs.getString("player_name", null)?.ifBlank { null }

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

        // this is so that updates to the localClientNameOverride are reflected in the bottom sheet

        playerToShowInSheet.value = playerToShowInSheet.value
    }

    fun toggleSound() {
        val value = !soundsEnabled
        prefs
            .edit()
            .putBoolean("sounds", value)
            .apply()
        sounds.isEnabled = value
        soundsEnabledLiveData.value = value
    }

    fun vibrate(milliseconds: Long) {
        @Suppress("DEPRECATION")
        if (vibrateOnMove)
            vibrator?.vibrate(milliseconds)
    }

    @UiThread
    fun setClient(client: GameClient) {
        if (client === this.client) return

        notificationManager?.shutdown()
        notificationManager = null

        this.client?.removeObserver(this)
        this.client = client
        client.addObserver(this)

        if (showNotifications) {
            // registers itself to the game and listens for events
            notificationManager = MultiplayerNotificationManager(context, client).apply {
                client.addObserver(this)
            }
        }

        connectionStatus.value = if (client.isConnected()) ConnectionStatus.Connected else ConnectionStatus.Disconnected
    }

    fun onStart() {
        notificationManager?.stopBackgroundNotification()
    }

    fun onStop() {
        notificationManager?.startBackgroundNotification()
    }

    @UiThread
    fun startConnectingClient(config: GameConfig, clientName: String?, onConnected: Runnable? = null) {
        val client = client ?: return
        Log.d(tag, "startConnectingClient")
        connectThread?.interrupt()
        connectThread?.join(100)

        connectionStatus.value = ConnectionStatus.Connecting
        setSheetPlayer(-1, false)

        connectThread = thread(name = "ConnectionThread") {
            val name = config.server ?: "(null)"
            Crashlytics.log(Log.INFO, tag, "Connecting to: $name")
            Crashlytics.setString("server", name)

            try {
                // client will notify observers about connection failed
                if (!client.connect(context, config.server, GameClient.DEFAULT_PORT)) {
                    // connection has failed, observers have been notified
                    connectionStatus.postValue(ConnectionStatus.Failed)
                    connectThread = null
                    Log.d(tag, "Connection failed")
                    return@thread
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                connectThread = null
                return@thread
            }

            Log.d(tag, "Connection successful")

            if (config.requestPlayers == null) {
                client.requestPlayer(-1, clientName)
            } else {
                for (i in 0..3)
                    if (config.requestPlayers[i])
                        client.requestPlayer(i, clientName)
            }
            if (config.showLobby) {
                if (config.server == null) {
                    appendServerInterfacesToChat()

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

            handler.post {
                connectionStatus.value = ConnectionStatus.Connected
                onConnected?.run()
            }

            connectThread = null
        }
    }

    @UiThread
    fun startConnectingBluetooth(remote: BluetoothDevice, clientName: String?) {
        val client = client ?: return
        val config = client.config
        connectThread?.interrupt()
        connectThread?.join(100)
        connectionStatus.value = ConnectionStatus.Connecting

        Crashlytics.log(Log.INFO, tag, "Connecting to bluetooth device")

        connectThread = thread(name = "BluetoothConnectThread") {
            try {
                Log.i(tag, "Connecting to " + remote.name + "/" + remote.address)
                if (!client.connect(context, remote)) {
                    // connection has failed, observers have been notified
                    connectionStatus.postValue(ConnectionStatus.Failed)
                    connectThread = null
                    return@thread
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                connectThread = null
                return@thread
            }

            Log.i(tag, "Connection successful")

            analytics.logEvent("bluetooth_connected", null)

            if (config.requestPlayers == null) {
                client.requestPlayer(-1, clientName)
            } else {
                for (i in 0..3)
                    if (config.requestPlayers[i])
                        client.requestPlayer(i, clientName)
            }

            connectionStatus.postValue(ConnectionStatus.Connected)

            connectThread = null
        }
    }

    @UiThread
    fun disconnectClient() {
        Log.d(tag, "disconnectClient")
        connectThread?.interrupt()
        connectThread = null
        val c = this.client
        this.client = null
        c?.disconnect()

        connectionStatus.value = ConnectionStatus.Disconnected
    }

    @WorkerThread
    private fun appendServerInterfacesToChat() {
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
     * Set the override of the player to show, when rotating the board.
     *
     * @param player the new player to show
     * @param isRotated whether the board is rotated or not
     */
    fun setSheetPlayer(player: Int, isRotated: Boolean) {
        playerToShowInSheet.postValue(SheetPlayer(player, isRotated))
    }

    /**
     * Returns the display name of the given player/color.
     *
     * This depends on whether this is a local player (use [localClientNameOverride]),
     * a player with a name in the [lastStatus], or the name of the color for the current game mode
     */
    fun getPlayerName(player: Int): String {
        val gameMode = client?.game?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS
        val colorName = Global.getColorName(context, player, gameMode)
        val game = client?.game ?: return colorName

        // always return the current override, so that changing the name in the preferences trumps what the server believes
        localClientNameOverride?.let {
            if (game.isLocalPlayer(player)) return it
        }

        // then either the name of the player, or the name of the color if not
        return lastStatus?.getPlayerName(player) ?: colorName
    }

    //region GameEventObserver callbacks

    override fun onConnected(client: GameClient) {
        Log.d(tag, "onConnected")
        lastStatus = null
        connectionStatus.postValue(ConnectionStatus.Connected)
        playerToShowInSheet.postValue(SheetPlayer(client.game.currentPlayer, false))
        canRequestHint.postValue(client.game.isLocalPlayer() && client.game.isStarted && !client.game.isFinished)
        canRequestUndo.postValue(false)
    }

    override fun gameStarted() {
        // this is so we get to update our [localClientNameOverride], because
        // we start a new local game without any player name, and it allows the
        // lobby to set a new name in the preferences before game start.
        handler.post {
            reloadPreferences()
        }
    }

    override fun serverStatus(status: MessageServerStatus) {
        this.lastStatus = status
    }

    override fun newCurrentPlayer(player: Int) {
        val client = client ?: return
        if (playerToShowInSheet.value?.isRotated == true) {
            // just re-post the same value, because the board is rotated and we need to update the view
            playerToShowInSheet.postValue(playerToShowInSheet.value)
        } else {
            setSheetPlayer(player, false)
        }

        canRequestHint.postValue(client.game.isLocalPlayer() && client.game.isStarted)
        canRequestUndo.postValue(
            client.game.isLocalPlayer() &&
            client.game.isStarted &&
            !client.game.isFinished &&
            (lastStatus?.clients == 1) &&
            !client.game.history.isEmpty()
        )
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
        Log.d(tag, "onDisconneced")
        if (client === this.client) {
            // we may already have swapped to another client, which drives the status
            lastStatus = null
            connectionStatus.postValue(ConnectionStatus.Disconnected)
            setSheetPlayer(-1, false)
        }
        chatHistory.clear()
    }

    override fun onGoogleAccountSignedOut() {
        googleAccountSignedIn.value = false
    }

    override fun onGoogleAccountSignedIn(account: GoogleSignInAccount) {
        googleAccountSignedIn.value = true
        if (Global.IS_VIP) {
            gameHelper.unlock(context.getString(R.string.achievement_vip))
        }
    }

    //endregion
}