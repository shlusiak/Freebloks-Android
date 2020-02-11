package de.saschahlusiak.freebloks.network

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.utils.read
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Use [asSequence] or [readMessage] to read [Message] objects from a given InputStream.
 *
 * @param stream the InputStream to read from
 */
class MessageReader(private val stream: InputStream) {
    private var buffer = ByteBuffer.allocate(10)

    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    private fun readNextIntoBuffer() {
        var read: Int

        buffer.clear()
        read = stream.read(buffer, NET_HEADER.HEADER_SIZE)
        if (read == -1) throw EOFException("EOF when reading packet header")

        if (read < NET_HEADER.HEADER_SIZE)
            throw IOException(String.format("short read: %d out of %d", read, Header.HEADER_SIZE))

        buffer.position(0)
        val header = Header.from(buffer)

        if (header.size > buffer.capacity()) {
            buffer = ByteBuffer.allocate(header.size)
            // put the header back into the buffer after reallocating new space
            buffer.put(header)
        }

        var remaining = header.size - NET_HEADER.HEADER_SIZE
        while (remaining > 0) {
            read = stream.read(buffer, remaining)
            if (read == -1)
                throw EOFException("EOF when reading packet payload, $header, remaining $remaining")
            remaining -= read
        }
        buffer.flip()
    }

    /**
     * @param block if false, return null if not enough data is available on the stream
     */
    @Throws(IOException::class, EOFException::class, ProtocolException::class)
    @WorkerThread
    fun readMessage(): Message? {
        readNextIntoBuffer()
        return Message.from(buffer)
    }

    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    fun asSequence(): Sequence<Message> {
        return sequence {
            try {
                while (true) {
                    readNextIntoBuffer()
                    val message = Message.from(buffer) ?: continue
                    yield(message)
                }
            } catch (e: EOFException) {
                // EOF
            }
        }
    }
}