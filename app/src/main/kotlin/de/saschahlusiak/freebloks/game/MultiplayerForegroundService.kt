package de.saschahlusiak.freebloks.game

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MultiplayerForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()

        Log.d(tag, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand")

        if (intent == null) {
            stopSelf()
        } else {
            val notification = intent.getParcelableExtra<Notification>("notification")
            startForeground(MultiplayerNotificationManager.ONGOING_NOTIFICATION_ID, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    companion object {
        private val tag = MultiplayerForegroundService::class.simpleName
    }
}