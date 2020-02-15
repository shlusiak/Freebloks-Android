package de.saschahlusiak.freebloks.client

import android.util.Log
import com.crashlytics.android.Crashlytics
import de.saschahlusiak.freebloks.model.GameStateException
import de.saschahlusiak.freebloks.network.MessageReader
import de.saschahlusiak.freebloks.network.ProtocolException
import java.io.IOException
import java.io.InputStream

/**
 * This Thread will continuously poll the client's InputStream to process individual messages.
 */
class GameClientThread(private val inputStream: InputStream, val client: GameClient) : Thread("GameClientThread") {
    private val tag = GameClientThread::class.java.simpleName

    @get:Synchronized
    private var goDown = false

    @get:Synchronized
    var error: Exception? = null
        private set

    @Synchronized
    fun goDown() {
        goDown = true
    }

    override fun run() {
        val reader = MessageReader(inputStream)
        try {
            for (message in reader) {
                if (goDown) return
                client.handleMessage(message)
            }
        } catch (e: GameStateException) {
            throw RuntimeException(e)
        } catch (e: ProtocolException) {
            throw RuntimeException(e)
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