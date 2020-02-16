package de.saschahlusiak.freebloks.bluetooth

import android.bluetooth.BluetoothSocket
import de.saschahlusiak.freebloks.client.GameClient
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

/**
 * Thread that connects to a local TCP socket and spawns two more threads to copy data between them:
 * - BluetoothReader to read from the bluetooth socket, writing to the TCP socket,
 * - SocketReader to read from the socket and write to the bluetooth socket
 *
 * @param remote the connected bluetooth socket
 * @param hostname host to connect to (localhost)
 * @param port port to connect to
 */
class BluetoothClientBridge(
    private var remote: BluetoothSocket?,
    private val hostname: String,
    private val port: Int = GameClient.DEFAULT_PORT
) : Thread("BluetoothClientBridge")
{
    private var local: Socket? = null

    override fun run() {
        try {
            val localAddress: SocketAddress = InetSocketAddress(hostname, port)
            local = Socket().apply {
                connect(localAddress, 3000)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            try {
                remote?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            return
        }

        val local = local ?: return
        val remote = remote ?: return

        fun copyTo(input: InputStream, output: OutputStream) {
            try {
                input.copyTo(output)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            shutdownSockets()
        }

        val bluetoothThread = Thread({ copyTo(remote.inputStream, local.outputStream) }, "BluetoothReader")
        val socketThread = Thread({ copyTo(local.inputStream, remote.outputStream) }, "SocketReader")

        bluetoothThread.start()
        socketThread.start()

        try {
            socketThread.join()
            bluetoothThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        shutdownSockets()
    }

    @Synchronized
    private fun shutdownSockets() {
        try {
            local?.close()
            local = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            remote?.close()
            remote = null
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}