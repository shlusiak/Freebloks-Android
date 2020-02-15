package de.saschahlusiak.freebloks.client

import android.util.Log
import com.crashlytics.android.Crashlytics
import de.saschahlusiak.freebloks.model.GameStateException
import de.saschahlusiak.freebloks.network.MessageReader
import de.saschahlusiak.freebloks.network.ProtocolException
import io.fabric.sdk.android.Fabric
import java.io.IOException
import java.io.InputStream

/**
 * This Thread will continuously poll the client's InputStream to process individual messages.
 *
 * @param inputStream the input stream to consume
 * @param handler where the received messages are passed to
 * @param onGoDown callback when the thread is going down, e.g. because of disconnect
 */
class GameClientThread(
    private val inputStream: InputStream,
    private val handler: NetworkEventHandler,
    private val onGoDown: () -> Unit
) : Thread("GameClientThread") {
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
                handler.handleMessage(message)
            }
        } catch (e: GameStateException) {
            throw RuntimeException(e)
        } catch (e: ProtocolException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            if (goDown) return

            if (Fabric.isInitialized()) Crashlytics.logException(e)
            e.printStackTrace()
            synchronized(this) {
                error = e
            }
        } finally {
            // connection is lost or whatever, shut down the client
            onGoDown.invoke()
            Log.i(tag, "disconnected, thread going down")
        }
    }
}