package de.saschahlusiak.freebloks.game

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.lobby.ChatEntry
import de.saschahlusiak.freebloks.lobby.ChatEntry.Companion.serverMessage
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.view.model.Sounds

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app), GameEventObserver {
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

    // client data
    var client: GameClient?
        private set

    // todo: is required?
    var lastStatus: MessageServerStatus? = null
        private set

    val game get() = client?.game

    val board get() = client?.game?.board

    val chatHistory = mutableListOf<ChatEntry>()

    val sounds = Sounds(app)

    // LiveData
    val chatHistoryAsLiveData = MutableLiveData(chatHistory)
    val soundsEnabledLiveData = MutableLiveData(sounds.isEnabled)

    init {
        client = null
        reloadPreferences()
    }

    override fun onCleared() {
        disconnectClient()
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
    }

    fun onStart() {
        notificationManager?.stopBackgroundNotification()
    }

    fun onStop() {
        notificationManager?.startBackgroundNotification()
    }

    fun disconnectClient() {
        client?.disconnect()
        client = null
    }

    //region GameEventObserver callbacks

    override fun onConnected(client: GameClient) {
        lastStatus = null
    }

    override fun serverStatus(status: MessageServerStatus) {
        this.lastStatus = status
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
        chatHistory.clear()
    }

    //endregion
}