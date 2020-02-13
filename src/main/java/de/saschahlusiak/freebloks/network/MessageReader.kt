package de.saschahlusiak.freebloks.network

import androidx.annotation.WorkerThread
import de.saschahlusiak.freebloks.utils.read
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Use [asSequence] or [readMessage] to read [Message] objects from a given InputStream.
 */
class MessageReader() {
    private var buffer = ByteBuffer.allocate(256)

    /**
     * Block and read a message into [buffer]
     */
    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    private fun readNextIntoBuffer(stream: InputStream) {
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
            buffer.put(header)
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
     * Blocks and reads a single message into [buffer], then returns the unmarshalled message.
     *
     * @param stream InputStream to suck on
     * @return message or null if unknown type
     */
    @Throws(IOException::class, EOFException::class, ProtocolException::class)
    @WorkerThread
    fun readMessage(stream: InputStream): Message? {
        readNextIntoBuffer(stream)
        return Message.from(buffer)
    }

    /**
     * Read messages as a sequence. The sequence will finish when [EOFException] is thrown on the stream.
     * Unknown message types will be silently skipped.
     *
     * @param stream InputStream to suck on
     */
    @Throws(IOException::class, ProtocolException::class)
    @WorkerThread
    fun asSequence(stream: InputStream): Sequence<Message> {
        return sequence {
            try {
                while (true) {
                    readNextIntoBuffer(stream)
                    val message = Message.from(buffer) ?: continue
                    yield(message)
                }
            } catch (e: EOFException) {
                // EOF
            }
        }
    }
}