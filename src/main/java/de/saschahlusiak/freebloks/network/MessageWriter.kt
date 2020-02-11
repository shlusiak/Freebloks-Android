package de.saschahlusiak.freebloks.network

import androidx.annotation.WorkerThread
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

class MessageWriter(val os: OutputStream) {
    /**
     * Sends the given messages as raw bytes to the [OutputStream]
     *
     * @param list of messages to send
     * @return true if all messages were sent successfully, false on IOException (broken pipe)
     */
    @WorkerThread
    fun send(vararg messages: Message): Boolean {
        messages.forEach { message ->
            val bytes = ByteArray(message.header.size)

            // message gets marshaled into the buffer
            message.write(ByteBuffer.wrap(bytes))

            try {
                os.write(bytes)
            } catch (e: IOException) {
                // this is usually a broken pipe exception, which happens when the connection
                // is closed. This is non-fatal here and does not need to be logged.
                return false
            }
        }

        return true
    }
}