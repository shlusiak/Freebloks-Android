package de.saschahlusiak.freebloks.network

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.utils.read
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Use this iterable to read [Message] objects from the given [stream].
 *
 * When the stream finishes, the iterator will throw an [EOFException].
 */
class MessageReader(private val stream: InputStream): Iterable<Message> {
    /**
     * the buffer will grow as required when reading messages.
     */
    private var buffer = ByteBuffer.allocate(8)

    /**
     * Block and read a message into [buffer]
     */
    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    private fun readNextIntoBuffer() {
        var read: Int

        buffer.clear()
        read = stream.read(buffer, Header.HEADER_SIZE)
        if (read == -1) throw EOFException("EOF when reading packet header")

        if (read < Header.HEADER_SIZE)
            throw IOException(String.format("short read: %d out of %d", read, Header.HEADER_SIZE))

        buffer.position(0)
        val header = Header.from(buffer)

        if (header.size > buffer.capacity()) {
            buffer = ByteBuffer.allocate(header.size)
            // put the header back into the buffer after reallocating new space
            header.write(buffer)
        }

        var remaining = header.size - Header.HEADER_SIZE
        while (remaining > 0) {
            read = stream.read(buffer, remaining)
            if (read == -1)
                throw EOFException("EOF when reading packet payload, $header, remaining $remaining")
            remaining -= read
        }
        buffer.flip()
    }

    /**
     * Produce a sequence of message. The sequence will not stop naturally and will throw an [EOFException] at the end.
     *
     * Unknown message types will be silently skipped.
     */
    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    private fun asSequence(): Sequence<Message> {
        return sequence {
            while (true) {
                readNextIntoBuffer()
                val message = Message.from(buffer) ?: continue
                yield(message)
            }
        }
    }

    override fun iterator(): Iterator<Message> {
        return asSequence().iterator()
    }
}