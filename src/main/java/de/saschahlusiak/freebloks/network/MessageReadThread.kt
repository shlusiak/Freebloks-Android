package de.saschahlusiak.freebloks.network

import android.util.Log
import com.crashlytics.android.Crashlytics
import de.saschahlusiak.freebloks.model.GameStateException
import io.fabric.sdk.android.Fabric
import java.io.IOException
import java.io.InputStream

/**
 * This Thread will continuously poll the client's InputStream to process individual messages.
 *
 * An IOException thrown inside the loop will cause the Thread to go down. The caught exception
 * is stored in [error].
 *
 * A [ProtocolException] or [GameStateException] will cause the Thread to throw a RuntimeException.
 *
 * @param inputStream the input stream to consume
 * @param handler where the received messages are passed to
 * @param onGoDown callback when the thread is going down, e.g. because of disconnect
 */
class MessageReadThread(
    private val inputStream: InputStream,
    private val handler: MessageHandler,
    private val onGoDown: () -> Unit
) : Thread("GameClientThread") {
    private val tag = MessageReadThread::class.java.simpleName

    @get:Synchronized
    private var goDown = false

    @get:Synchronized
    var error: Exception? = null
        private set

    @Synchronized
    fun goDown() {
        goDown = true
        interrupt()
    }

    override fun run() {
        val reader = MessageReader(inputStream)
        try {
            for (message in reader) {
                if (goDown) return
                handler.handleMessage(message)
            }
        } catch (e: GameStateException) {
            e.printStackTrace()
            synchronized(this) {
                error = e
            }
            // this RuntimeException is fatal and supposed to terminate the app
            throw RuntimeException(e)
        } catch (e: ProtocolException) {
            e.printStackTrace()
            synchronized(this) {
                error = e
            }
            // this RuntimeException is fatal and supposed to terminate the app
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