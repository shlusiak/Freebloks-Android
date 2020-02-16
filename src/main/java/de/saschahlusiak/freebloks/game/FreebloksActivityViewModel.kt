package de.saschahlusiak.freebloks.game

import android.app.Application
import android.content.Context
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.lobby.ChatEntry
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app), GameEventObserver {
    private val context = app

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    var vibrateOnMove: Boolean = false
        private set

    var client: GameClient?
        private set

    var lastStatus: MessageServerStatus? = null
        private set

    val game get() = client?.game

    val board get() = client?.game?.board

    val chatHistory = mutableListOf<ChatEntry>()

    init {
        client = null
        reloadPreferences()
    }

    fun reloadPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())

        vibrateOnMove = prefs.getBoolean("vibrate", true)
    }

    fun vibrate(milliseconds: Long) {
        if (vibrateOnMove)
            vibrator?.vibrate(milliseconds)
    }

    fun setClient(client: GameClient) {
        this.client = client
        client.addObserver(this)
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

    override fun chatReceived(client: Int, message: String) {
        val name: String
        var player = -1
        val status = lastStatus
        if (status != null && client >= 0) {
            player = status.clientForPlayer.indexOfFirst { it == client }

            name = status.getClientName(context.resources, client)
        } else {
            // if we have advanced status, ignore all server messages (c == -1)
            // server messages are synthetically generated in FreebloksActivity.serverStatus()
            if (status != null) return
            // without a status, the name is the client name, not the player name
            name = context.getString(R.string.client_d, client + 1)
        }

        val e = ChatEntry.clientMessage(client, player, message, name)

        chatHistory.add(e)
    }

    override fun onDisconnected(client: GameClient, error: Exception?) {
        lastStatus = null
        chatHistory.clear()
    }
    //endregion

}