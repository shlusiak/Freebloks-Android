package de.saschahlusiak.freebloks.game

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.DependencyProvider
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.game.lobby.ChatItem
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothClientToSocketThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread
import de.saschahlusiak.freebloks.network.bluetooth.BluetoothServerThread.OnBluetoothConnectedListener
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.theme.BaseSounds
import de.saschahlusiak.freebloks.theme.DefaultSounds
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import de.saschahlusiak.freebloks.view.scene.AnimationType
import de.saschahlusiak.freebloks.view.scene.SceneDelegate
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.coroutines.coroutineContext

enum class ConnectionStatus {
    Disconnected, Connecting, Connected, Failed
}

data class SheetPlayer(
    // the player to show on the board or -1
    val player: Int,
    // whether the board is currently rotated or in its "home" position.
    val isRotated: Boolean
)

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app), GameEventObserver, SceneDelegate {
    private val tag = FreebloksActivityViewModel::class.java.simpleName
    private val context = app
    private val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())
    private val analytics by lazy { DependencyProvider.analytics() }

    // services
    private var notificationManager: MultiplayerNotificationManager? = null

    // settings
    private var showNotifications: Boolean = true
    var showIntro = true
    val soundsEnabled get() = sounds.soundsEnabled
    // TODO: I think we should ditch this override completely and only support what was set during game start. What do you think?
    var localClientNameOverride: String? = null
        private set
    var showSeeds = false
    var showOpponents = false
    var snapAid = false
    var showAnimations = AnimationType.Full

    // other stuff
    var intro: Intro? = null
    private var connectJob: Job? = null
    val gameHelper: GooglePlayGamesHelper

    // client data
    var client: GameClient?
        private set
    val game get() = client?.game
    val board get() = client?.game?.board
    var lastStatus: MessageServerStatus? = null
        private set

    private val chatHistory = mutableListOf<ChatItem>()
    val sounds: BaseSounds = DefaultSounds(app)

    // LiveData
    val chatHistoryAsLiveData = MutableLiveData<List<ChatItem>>(chatHistory)
    val soundsEnabledLiveData = MutableLiveData(sounds.soundsEnabled)
    val chatButtonVisible = MutableLiveData(false)
    val connectionStatus = MutableLiveData(ConnectionStatus.Disconnected)
    val playerToShowInSheet = MutableLiveData(SheetPlayer(-1, false))
    val googleAccountSignedIn: MutableLiveData<Boolean>
    val canRequestUndo = MutableLiveData(false)
    val canRequestHint = MutableLiveData(false)
    val inProgress = MutableLiveData(false)

    init {
        client = null
        gameHelper = DependencyProvider.googlePlayGamesHelper()
        googleAccountSignedIn = gameHelper.signedIn
        reloadPreferences()
    }

    override fun onCleared() {
        disconnectClient()
        notificationManager?.shutdown()
        notificationManager = null
        sounds.shutdown()
    }

    @UiThread
    fun reloadPreferences() {
        with(prefs) {
            sounds.vibrationEnabled = getBoolean("vibrate", true)
            sounds.soundsEnabled = getBoolean("sounds", true)
            showNotifications = getBoolean("notifications", true)
            localClientNameOverride = getString("player_name", null)?.ifBlank { null }
            showSeeds = getBoolean("show_seeds", true)
            showOpponents = getBoolean("show_opponents", true)
            val animationType = getString("animations", AnimationType.Full.settingsValue.toString())?.toInt() ?: 0
            showAnimations = AnimationType.values().firstOrNull { it.settingsValue == animationType } ?: AnimationType.Full
            snapAid = getBoolean("snap_aid", true)
            showIntro = !getBoolean("skip_intro", false)
        }

        soundsEnabledLiveData.value = sounds.soundsEnabled

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

    fun toggleSound(): Boolean {
        val value = !soundsEnabled
        prefs
            .edit()
            .putBoolean("sounds", value)
            .apply()
        sounds.soundsEnabled = value
        soundsEnabledLiveData.value = value
        return value
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
        saveGameState()
    }

    fun saveGameState(filename: String = GAME_STATE_FILE) {
        val client = client ?: return
        val game = client.game

        if (!game.isStarted || game.isFinished) return

        GlobalScope.launch(Dispatchers.IO) {
            val p = Parcel.obtain()

            try {
                synchronized(client) {
                    val b = Bundle()
                    b.putSerializable("game", game)
                    p.writeBundle(b)
                }

                context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(p.marshall())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                p.recycle()
            }
        }
    }

    fun loadGameState(filename: String = GAME_STATE_FILE): Game? {
        val bytes = ByteArrayOutputStream().use { output ->
            context.openFileInput(filename).use { input ->
                input.copyTo(output)
            }
            output.toByteArray()
        }

        val p = Parcel.obtain()
        p.unmarshall(bytes, 0, bytes.size)
        p.setDataPosition(0)

        val bundle = p.readBundle(javaClass.classLoader)
        p.recycle()

        val game = bundle?.getSerializable("game") as Game? ?: return null
        if (game.isFinished) return null

        viewModelScope.launch(Dispatchers.IO) {
            context.deleteFile(filename)
        }

        return game
    }

    @UiThread
    suspend fun connectToHost(config: GameConfig, clientName: String?, requestGameStart: Boolean) {
        val client = client ?: return
        Log.d(tag, "startConnectingClient")

        withTimeoutOrNull(200) {
            connectJob?.cancelAndJoin()
        }

        connectionStatus.value = ConnectionStatus.Connecting
        setSheetPlayer(-1, false)
        chatButtonVisible.value = false

        connectJob = coroutineContext[Job]

        val name = config.server ?: "(null)"
        val crashReporting = DependencyProvider.crashReporter()
        crashReporting.log("Connecting to: $name")
        crashReporting.setString("server", name)

        // client will notify observers about connection failed
        if (!client.connect(config.server, GameClient.DEFAULT_PORT)) {
            // connection has failed, observers have been notified
            connectionStatus.value = ConnectionStatus.Failed
            connectJob = null
            Log.d(tag, "Connection failed")
            return
        }

        Log.d(tag, "Connection successful")

        if (config.requestPlayers == null) {
            client.requestPlayer(-1, clientName)
        } else {
            for (i in 0..3)
                if (config.requestPlayers[i])
                    client.requestPlayer(i, clientName)
        }

        if (config.showLobby && (config.server == null)) {
            appendServerInterfacesToChat()
            startBluetoothServer(client)
        }

        connectionStatus.value = ConnectionStatus.Connected

        if (requestGameStart) {
            client.requestGameStart()
        }
    }

    private fun startBluetoothServer(client: GameClient) {
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

    @UiThread
    fun connectToBluetooth(remote: BluetoothDevice, clientName: String?) = viewModelScope.launch {
        val client = client ?: return@launch
        val config = client.config

        withTimeoutOrNull(200) {
            connectJob?.cancelAndJoin()
        }

        connectionStatus.value = ConnectionStatus.Connecting
        chatButtonVisible.value = false

        DependencyProvider.crashReporter().log("Connecting to bluetooth device")

        connectJob = coroutineContext[Job]

        Log.i(tag, "Connecting to " + remote.name + "/" + remote.address)
        if (!client.connect(remote)) {
            // connection has failed, observers have been notified
            connectionStatus.postValue(ConnectionStatus.Failed)
            connectJob = null
            return@launch
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
    }

    @UiThread
    fun disconnectClient() {
        Log.d(tag, "disconnectClient")
        connectJob?.cancel()
        val c = this.client
        this.client = null
        c?.disconnect()

        setSheetPlayer(-1, false)
        connectionStatus.value = ConnectionStatus.Disconnected
    }

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

                    val e = ChatItem.Generic(String.format("[%s]", a))
                    chatHistory.add(e)
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        chatHistoryAsLiveData.postValue(chatHistory.toList())
    }

    /**
     * Set the override of the player to show, when rotating the board.
     *
     * @param player the new player to show
     * @param isRotated whether the board is rotated or not
     */
    override fun setSheetPlayer(player: Int, isRotated: Boolean) {
        playerToShowInSheet.postValue(SheetPlayer(player, isRotated))
    }

    override fun commitCurrentStone(turn: Turn) {
        client?.setStone(turn)
    }

    /**
     * Returns the display name of the given player/color.
     *
     * This depends on whether this is a local player (use [localClientNameOverride]),
     * a player with a name in the [lastStatus], or the name of the color for the current game mode
     */
    fun getPlayerName(player: Int): String {
        val gameMode = client?.game?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS
        val color = gameMode.colorOf(player)
        val colorName = color.getName(context.resources)
        val game = client?.game ?: return colorName

        // always return the current override, so that changing the name in the preferences trumps what the server believes
        localClientNameOverride?.let {
            if (game.isLocalPlayer(player)) return it
        }

        // then either the name of the player, or the name of the color if not
        return lastStatus?.getPlayerName(player) ?: colorName
    }

    fun requestHint() {
        client?.run {
            inProgress.value = true
            canRequestHint.value = false
            requestHint()
        }
    }

    fun requestUndo() {
        client?.requestUndo()
    }

    //region GameEventObserver callbacks

    @UiThread
    override fun onConnected(client: GameClient) {
        Log.d(tag, "onConnected")
        lastStatus = null
        connectionStatus.value = ConnectionStatus.Connected
        playerToShowInSheet.value = SheetPlayer(client.game.currentPlayer, false)
        canRequestHint.value = (client.game.isLocalPlayer() && client.game.isStarted && !client.game.isFinished)
        canRequestUndo.value = false
    }

    @WorkerThread
    override fun gameStarted() {
        // this is so we get to update our [localClientNameOverride], because
        // we start a new local game without any player name, and it allows the
        // lobby to set a new name in the preferences before game start.
        viewModelScope.launch {
            reloadPreferences()
        }

        // Send analytics event
        val lastStatus = lastStatus ?: return

        val client = client ?: return
        val game = client.game

        val b = Bundle().apply {
            putString("server", client.config.server ?: "")
            putString("game_mode", game.gameMode.toString())
            putInt("w", game.board.width)
            putInt("h", game.board.height)
            putInt("clients", lastStatus.clients)
            putInt("players", lastStatus.player)
        }

        analytics.logEvent("game_started", b)
        if (lastStatus.clients >= 2) {
            analytics.logEvent("game_start_multiplayer", b)
        }
    }

    @WorkerThread
    override fun serverStatus(status: MessageServerStatus) {
        this.lastStatus = status

        if (status.clients > 1) {
            chatButtonVisible.postValue(true)
        }
    }

    @WorkerThread
    override fun gameFinished() {
        super.gameFinished()

        viewModelScope.launch {
            context.deleteFile(GAME_STATE_FILE)
        }
    }

    @WorkerThread
    override fun newCurrentPlayer(player: Int) {
        val client = client ?: return
        if (playerToShowInSheet.value?.isRotated == true) {
            // just re-post the same value, because the board is rotated and we need to update the view
            playerToShowInSheet.postValue(playerToShowInSheet.value)
        } else {
            setSheetPlayer(player, false)
        }

        inProgress.postValue(!client.game.isLocalPlayer())

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
        val isLocal = game?.isLocalPlayer(player) ?: false
        val e = ChatItem.Message(client, if (player < 0) null else player, isLocal, name, message)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory.toList())
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {
        val gameMode = game?.gameMode ?: GameMode.DEFAULT
        val clientName = name ?: context.getString(R.string.client_d, client + 1)

        val playerColor = gameMode.colorOf(player)
        val colorName = playerColor.getName(context.resources)

        val text = context.getString(R.string.player_joined_color, clientName, colorName)
        val e = ChatItem.Server(player, text)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory.toList())
    }

    override fun playerLeft(client: Int, player: Int, name: String?) {
        val clientName = name ?: context.getString(R.string.client_d, client + 1)
        val gameMode = game?.gameMode ?: GameMode.DEFAULT

        val playerColor = gameMode.colorOf(player)
        val colorName = playerColor.getName(context.resources)

        val text = context.getString(R.string.player_left_color, clientName, colorName)
        val e = ChatItem.Server(player, text)

        chatHistory.add(e)
        chatHistoryAsLiveData.postValue(chatHistory.toList())
    }

    override fun hintReceived(turn: Turn) {
        inProgress.postValue(false)
        canRequestHint.postValue(client?.game?.isStarted ?: false)
    }

    @WorkerThread
    override fun stoneUndone(t: Turn) {
        analytics.logEvent("game_undo", null)
    }

    override fun onDisconnected(client: GameClient, error: Exception?) {
        Log.d(tag, "onDisconneced")
        if (client === this.client) {
            // we may already have swapped to another client, which drives the status
            lastStatus = null
            connectionStatus.postValue(ConnectionStatus.Disconnected)
            setSheetPlayer(-1, false)
            chatButtonVisible.postValue(false)
        }
        chatHistory.clear()
    }

    //endregion

    companion object {
        private const val GAME_STATE_FILE = "gamestate.bin"
    }
}