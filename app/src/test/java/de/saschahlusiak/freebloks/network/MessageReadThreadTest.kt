package de.saschahlusiak.freebloks.network

import de.saschahlusiak.freebloks.client.GameClientMessageHandler
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameStateException
import de.saschahlusiak.freebloks.network.message.MessageCurrentPlayer
import de.saschahlusiak.freebloks.utils.ubyteArrayOf
import org.junit.Assert
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.PipedInputStream
import java.io.PipedOutputStream

class MessageReadThreadTest {

    private val dataIncomplete = ubyteArrayOf(
        //region data
        0x06, 0x00, 0xab, 0x07, 0xd7, 0x00, 0x04, 0x01, 0x14, 0x14, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 0xfe, 0xfe, 0xfe, 0xfe, 0x00, 0xae, 0x62, 0xc4, 0x01,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xb8, 0xab, 0x50, 0xbb, 0x00, 0x0b, 0x15, 0xee, 0x04, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x40, 0xc4, 0xf4, 0xac,
        0x85, 0xd8, 0x00, 0xac, 0x85, 0xd8, 0x00, 0x00, 0x00, 0x00, 0x40, 0x91, 0x22, 0xee, 0x01, 0x00, 0x00, 0x00, 0x00, 0xac, 0x50, 0xbb, 0x38, 0xac, 0x50,
        0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xab, 0xa3, 0xa0, 0xa7, 0x00, 0x91, 0x22, 0xee, 0x46, 0xac, 0x50, 0xbb, 0xc4, 0xfd, 0x51, 0xbb, 0xe8, 0xab, 0x50, 0xbb,
        0x00, 0xae, 0x62, 0xc4, 0x18, 0x00, 0x00, 0x00, 0x34, 0x7c, 0x22, 0xee, 0xe8, 0xab, 0x50, 0xbb, 0x00, 0x83, 0x14, 0xee, 0xa0, 0xae, 0x62, 0xc4, 0x08,
        0x02, 0xff, 0xff, 0xf8, 0xab, 0x50, 0xbb, 0x00, 0x84, 0x14, 0xee, 0xff, 0x25, 0x19, 0xee, 0xc4, 0xfd, 0x51, 0xbb, 0x28, 0xac, 0x50, 0xbb, 0x03, 0x01,
        0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x06, 0x00, 0xab, 0x07,
        0xd7, 0x00, 0x04, 0x01, 0x14, 0x14, 0x01, 0x01, 0x01, 0x01, 0x01, 0x02, 0xfe, 0xfe, 0xfe, 0xfe, 0x00, 0x65, 0x51, 0xbb, 0xf8, 0x16, 0x89, 0xd8, 0x04,
        0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x65, 0x51, 0xbb, 0x14, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00
        //endregion
    )

    private val dataInvalidHeader = ubyteArrayOf(
        //region data
        0x07, 0x00, 0x06, 0x03, 0xd5, 0x03
        //endregion
    )

    private val dataInvalidGameState = ubyteArrayOf(
        //region data
        0x05, 0x00, 0x0b, 0x04, 0xd7, 0x01, 0x04, 0x00, 0x03, 0x04, 0x06
        //endregion
    )

    @Test
    fun test_goDown() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val input = PipedInputStream()
        val output = PipedOutputStream()
        input.connect(output)

        val writer = MessageWriter(output)
        val reader = MessageReadThread(input, handler) { wentDown = true }
        reader.start()
        writer.write(MessageCurrentPlayer(3))
        reader.goDown()
        reader.join(2000)

        assertTrue(wentDown)
        assertNull(reader.error)
    }

    @Test
    fun test_noData_EOF() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val thread = MessageReadThread(ByteArrayInputStream(ubyteArrayOf()), handler) { wentDown = true }
        thread.start()
        thread.join(5000)

        Assert.assertTrue(wentDown)
        Assert.assertTrue(thread.error is EOFException)
    }

    @Test
    fun test_completeMessage_EOF() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val thread = MessageReadThread(ByteArrayInputStream(dataIncomplete), handler) { wentDown = true }
        thread.start()
        thread.join(5000)

        Assert.assertTrue(wentDown)
        Assert.assertTrue(thread.error is EOFException)
    }

    @Test
    fun test_invalidHeader() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val thread = MessageReadThread(ByteArrayInputStream(dataInvalidHeader), handler) { wentDown = true }
        // the Thread will throw a RuntimeException(ProtocolException), which would normally terminate the app,
        // but here we can see it as a ProtocolException
        thread.start()
        thread.join(5000)

        Assert.assertTrue(wentDown)
        Assert.assertTrue(thread.error is ProtocolException)
    }

    @Test
    fun test_invalidGameState() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val thread = MessageReadThread(ByteArrayInputStream(dataInvalidGameState), handler) { wentDown = true }
        // the Thread will throw a RuntimeException(GameStateException), which would normally terminate the app,
        // but here we can see it as a GameStateException
        thread.start()
        thread.join(5000)

        Assert.assertTrue(wentDown)
        Assert.assertTrue(thread.error is GameStateException)
    }

    @Test
    fun test_goDown_before_data() {
        val handler = GameClientMessageHandler(Game())
        var wentDown = false

        val thread = MessageReadThread(ByteArrayInputStream(dataIncomplete), handler) { wentDown = true }
        thread.goDown()
        thread.start()
        thread.join(5000)

        Assert.assertTrue(wentDown)
        Assert.assertNull(thread.error)
    }
}