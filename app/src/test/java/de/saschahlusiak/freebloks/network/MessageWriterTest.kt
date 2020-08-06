package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.model.Orientation
import de.saschahlusiak.freebloks.network.message.MessageCurrentPlayer
import de.saschahlusiak.freebloks.network.message.MessageSetStone
import de.saschahlusiak.freebloks.utils.hexString
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PipedOutputStream

class MessageWriterTest {
    @Test
    fun test_writer_success() {
        val os = ByteArrayOutputStream()
        val writer = MessageWriter(os)

        val result = writer.write(
            MessageCurrentPlayer(1),
            MessageSetStone(1, 3, Orientation.Default, 5, 7)
        )
        assertTrue(result)

        val bytes = os.toByteArray()
        assertNotNull(bytes)

        println(bytes.hexString())
        val expected = ubyteArrayOf(
            0x07, 0x00, 0x06, 0x03, 0xd4,
            0x01, 0x05, 0x00, 0x0b, 0x04, 0xd7, 0x01, 0x03, 0x00, 0x00, 0x05, 0x07
        )

        assertArrayEquals(expected, bytes)
    }

    @Test
    fun test_writer_closed() {
        val os = PipedOutputStream()
        val writer = MessageWriter(os)
        os.close()

        val result = writer.write(
            MessageCurrentPlayer(1),
            MessageSetStone(1, 3, Orientation.Default, 5, 7)
        )
        assertFalse(result)
    }
}