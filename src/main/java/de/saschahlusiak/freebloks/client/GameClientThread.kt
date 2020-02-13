package de.saschahlusiak.freebloks.client

import android.util.Log
import com.crashlytics.android.Crashlytics
import java.io.IOException

/**
 * This Thread will continuously call [GameClient.poll] to process individual messages.
 */
class GameClientThread(val client: GameClient) : Thread("GameClientThread") {
    private val tag = GameClientThread::class.java.simpleName

    @get:Synchronized
    private var goDown = false

    @get:Synchronized
    var error: Exception? = null
        private set

    @Synchronized
    fun goDown() {
        goDown = true
        client.disconnect()
    }

    override fun run() {
        try {
            do {
                // block to fetch and process a single message
                client.poll()
                if (goDown) return
            } while (client.isConnected)
        } catch (e: IOException) {
            if (goDown) return
            Crashlytics.logException(e)
            e.printStackTrace()
            synchronized(this) {
                error = e
            }
        } finally {
            client.disconnect()
            Log.i(tag, "disconnected, thread going down")
        }
    }
}