package de.saschahlusiak.freebloks.game

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.game.MultiplayerNotificationManager.Companion.ONGOING_NOTIFICATION_ID
import de.saschahlusiak.freebloks.utils.InstantAppHelper
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

/**
 * Class to manage the multi-player Android notification.
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
 * Instant Apps do not support notifications!
 */
@AndroidEntryPoint
class MultiplayerNotificationService : Service() {
    @Inject
    lateinit var instantAppHelper: InstantAppHelper

    private var client: GameClient? = null
    private var notificationManager: MultiplayerNotificationManager? = null

    /**
     * While the ongoing notification is running, we need a wakelock so we don't lose
     * connection with the server
     */
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        notificationManager?.shutdown()
        notificationManager = null
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
        super.onDestroy()
    }

    fun onActivityStart() {
        notificationManager?.stopBackgroundNotification()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        Log.d(tag, "Releasing wakeLock")
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun onActivityStop() {
        Log.d(tag, "Acquiring wakeLock")
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "freebloks:ongoing-notification").also {
            // 10 minutes should be plenty for the user to return to the app
            it.acquire(10.minutes.inWholeMilliseconds)
        }

        if (instantAppHelper.isInstantApp) return
        val notification = notificationManager?.getBackgroundNotification() ?: return

        Log.d(tag, "Starting foreground service")
        ServiceCompat.startForeground(
            this,
            ONGOING_NOTIFICATION_ID,
            notification,
            FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
    }

    fun setClient(client: GameClient?) {
        Log.d(tag, "setClient($client)")
        this.client = client
        notificationManager?.shutdown()
        notificationManager = null
        if (client != null && !instantAppHelper.isInstantApp) {
            notificationManager = MultiplayerNotificationManager(this, client)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(tag, "onUnbind")
        return super.onUnbind(intent)
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MultiplayerNotificationService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(tag, "onBind($intent)")
        return LocalBinder()
    }

    companion object {
        private val tag = MultiplayerNotificationService::class.java.simpleName
    }
}