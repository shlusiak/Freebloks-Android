package de.saschahlusiak.freebloks.game

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

/**
 * Class to manage the multi player Android notification.
 *
 * Will register itself to the given game and observe its events.
 *
 * We show a notification when:
 * - The game is in the background (onStop) and either
 *   - we are in the lobby
 *   - or we have 2 clients or more
 * - A chat is received and the game is started or in the background
 *
 * The notification is ongoing while the game is in the background.
 *
 * TODO: make notifications prettier
 */
class MultiplayerNotificationManager(val context: Context, val client: GameClient) : GameEventObserver {
    private val notificationManager = NotificationManagerCompat.from(context)

    private val game get() = client.game

    /**
     * Used to track whether we are currently showing a notification because we are in the background
     */
    private var isInBackground = false

    /**
     * We need to hold on to the last server status for player names
     */
    private var lastStatus: MessageServerStatus? = null

    /**
     * Immediately start observing game events.
     */
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
    }

    /**
     * Indicate that we are in the background and should show a long-running notification
     */
    fun startBackgroundNotification() {
        if (!client.isConnected()) return
        val lastStatus = lastStatus ?: return
        if (game.isStarted && lastStatus.clients == 1) return

        isInBackground = true
        notificationManager.notify(ONGOING_NOTIFICATION_ID, buildOngoingNotification(false))
    }

    /**
     * We are no longer in the background and should cancel the notifications
     */
    fun stopBackgroundNotification() {
        cancel()
        isInBackground = false
    }

    /**
     * Cancels any shown notification for the current and removes itself as an observer
     */
    fun shutdown() {
        client.removeObserver(this)
        cancel()
    }

    /**
     * Create any required notification channels
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val defaultChannel = NotificationChannel(CHANNEL_DEFAULT, context.getString(R.string.notification_channel_default), NotificationManager.IMPORTANCE_LOW)
        defaultChannel.enableVibration(false)
        defaultChannel.enableLights(false)
        notificationManager.createNotificationChannel(defaultChannel)

        val chatChannel = NotificationChannel(CHANNEL_CHAT, context.getString(R.string.chat), NotificationManager.IMPORTANCE_HIGH)
        chatChannel.enableVibration(true)
        chatChannel.enableLights(true)
        notificationManager.createNotificationChannel(chatChannel)
    }

    private fun buildOngoingNotification(withAlert: Boolean): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_DEFAULT)

        val contentIntent = Intent(Intent.ACTION_MAIN, null, context, FreebloksActivity::class.java).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val pendingContentIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.setContentIntent(pendingContentIntent)
        builder.addAction(android.R.drawable.ic_media_play, context.getString(R.string.notification_continue), pendingContentIntent)

        val disconnectIntent = Intent(context, FreebloksActivity::class.java).apply {
            action = Intent.ACTION_DELETE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra("disconnect", true)
        }

        val pendingDisconnectIntent = PendingIntent.getActivity(context, 1, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.notification_disconnect), pendingDisconnectIntent)

        builder.apply {
            setContentTitle(context.getString(R.string.app_name))
            setDefaults(0)
            setTicker(null)
            setAutoCancel(true)
            setSound(null)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setOngoing(!client.game.isFinished && client.isConnected())
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.appicon_small))
            setColor(context.resources.getColor(R.color.primaryColor))

            if (!client.game.isStarted) {
                // we are in lobby
                setSmallIcon(R.drawable.notification_icon)
                setContentText(context.getString(R.string.lobby_waiting_for_players))
                setTicker(context.getString(R.string.lobby_waiting_for_players))
            } else if (client.game.isFinished) {
                // game is over notification
                setSmallIcon(R.drawable.notification_icon)
                setContentText(context.getString(R.string.game_finished))
            } else {
                // current player / your turn notification
                val player = game.currentPlayer
                // note that player -1 is basically mapped to "White"
                val playerName = lastStatus?.getPlayerName(player) ?: Global.getColorName(context, player, game.gameMode)

                if (client.game.isLocalPlayer()) {
                    setSmallIcon(R.drawable.notification_icon)
                    setContentText(context.getString(R.string.your_turn, playerName))
                    setTicker(context.getString(R.string.your_turn, playerName))
                    if (withAlert) {
                        setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
                        setPriority(NotificationCompat.PRIORITY_HIGH)
                        setChannelId(CHANNEL_CHAT)
                    }
                } else {
                    setSmallIcon(R.drawable.notification_icon)
                    setContentText(context.getString(R.string.waiting_for_color, playerName))
                    setTicker(context.getString(R.string.waiting_for_color, playerName))
                }
            }
        }

        return builder.build()
    }

    private fun buildChatNotification(ongoing: Boolean = false, title: String?, text: String): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_CHAT)

        val contentIntent = Intent(Intent.ACTION_MAIN, null, context, FreebloksActivity::class.java).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra("showChat", true)
        }

        val pendingContentIntent = PendingIntent.getActivity(context, 1, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        builder.apply {
            setContentTitle(title ?: context.getString(R.string.app_name))
            setContentIntent(pendingContentIntent)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.appicon_small))

            setSmallIcon(R.drawable.notification_icon)
            setContentText(text)
            setTicker(text)
            setColor(context.resources.getColor(R.color.primaryColor))
            setPriority(NotificationCompat.PRIORITY_HIGH)
            setDefaults(NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_LIGHTS)
            setSound(soundUri)
            setOngoing(ongoing)
        }

        return builder.build()
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {
        if (!isInBackground) return

        val clientName = name ?: context.getString(R.string.client_d, client + 1)
        val colorName = Global.getColorName(context, player, game.gameMode)

        val text = context.getString(R.string.player_joined_color, clientName, colorName)

        if (game.isStarted) {
            notificationManager.notify(CHAT_NOTIFICATION_ID, buildChatNotification(ongoing = false, title = null, text = text))
        } else {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, buildChatNotification(ongoing = true, title = null, text = text))
        }
    }

    override fun playerLeft(client: Int, player: Int, name: String?) {
        if (!isInBackground && !game.isStarted) return
        if (game.isLocalPlayer(player)) return

        val clientName = name ?: context.getString(R.string.client_d, client + 1)
        val colorName = Global.getColorName(context, player, game.gameMode)

        val text = context.getString(R.string.player_left_color, clientName, colorName)

        if (game.isStarted) {
            if (isInBackground && lastStatus?.clients == 1) {
                stopBackgroundNotification()
            }

            notificationManager.notify(CHAT_NOTIFICATION_ID, buildChatNotification(ongoing = false, title = null, text = text))
        } else {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, buildChatNotification(ongoing = true, title = null, text = text))
        }
    }

    override fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {
        if (!isInBackground && !game.isStarted) return

        if (game.isLocalPlayer(player)) return

        val name = if (player >= 0)
            status.getPlayerName(player) ?: Global.getColorName(context, player, status.gameMode)
        else
            status.getClientName(client) ?: context.getString(R.string.client_d, client + 1)

        val title = context.getString(R.string.notification_title, name)
        notificationManager.notify(CHAT_NOTIFICATION_ID, buildChatNotification(ongoing = false, title = title, text = message))
    }

    override fun newCurrentPlayer(player: Int) {
        if (isInBackground) {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, buildOngoingNotification(game.isLocalPlayer()))
        }
    }

    override fun serverStatus(status: MessageServerStatus) {
        lastStatus = status
    }

    override fun onDisconnected(client: GameClient, error: Exception?) {
        shutdown()
    }

    /**
     * Cancel any currently shown notifications
     */
    private fun cancel() {
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
        notificationManager.cancel(CHAT_NOTIFICATION_ID)
        isInBackground = false
    }

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 2
        private const val CHAT_NOTIFICATION_ID = 3

        private const val CHANNEL_DEFAULT = "default"
        private const val CHANNEL_CHAT = "chat"

        private val soundUri = Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${R.raw.chat}")
    }
}