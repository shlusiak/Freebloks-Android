package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.network.message.MessageGameFinish
import de.saschahlusiak.freebloks.network.message.MessageStartGame
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException

class MessageReaderTest {
    private fun MessageReader.readAll(): List<Message> {
        val messages = mutableListOf<Message>()
        try {
            messages.addAll(this)
        } catch (e: EOFException) {
            // pass
        }
        return messages
    }

    @Test
    fun test_readSequence_empty() {
        val reader = MessageReader(ByteArrayInputStream(byteArrayOf()))

        val messages = reader.readAll()

        assertEquals(0, messages.size)
    }

    @Test
    fun test_readSequence_unknown() {
        val bos = ByteArrayOutputStream()
        val writer = MessageWriter(bos)

        writer.write(
            MessageStartGame(),
            object: Message(59) {},
            MessageGameFinish()
        )

        val bytes = bos.toByteArray()
        val reader = MessageReader(ByteArrayInputStream(bytes))

        val messages = reader.readAll()

        assertEquals(2, messages.size)
    }
}