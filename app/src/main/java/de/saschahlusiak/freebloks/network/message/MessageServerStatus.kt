package de.saschahlusiak.freebloks.network.message

import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageType
import de.saschahlusiak.freebloks.utils.getArray
import de.saschahlusiak.freebloks.utils.put
import java.io.Serializable
import java.nio.ByteBuffer

data class MessageServerStatus(
    val player: Int,                // int8
    val computer: Int,              // int8
    val clients: Int,               // int8
    val width: Int,                 // int8
    val height: Int,                // int8
    val gameMode: GameMode,         // int8

    // since 1.5, optional
    // a map of player number to client number, or -1 if played by computer
    val clientForPlayer: Array<Int?>,          // int8[4]
    // names for each client. we don't have names for players, unfortunately.
    val clientNames: Array<String?>, // uint8[8][16]
    // since 1.6, optional
    // the version of this header
    val version: Int = VERSION_MAX,               // int8
    // the client should reject any version that is below this
    val minVersion: Int = VERSION_MAX,            // int8
    val stoneNumbers: IntArray = IntArray(21) { 1 } // int8[21]

) : Message(MessageType.ServerStatus, HEADER_SIZE_1_6), Serializable {

    init {
        assert(player in 0..4) { "Invalid number of players $player"}
        assert(computer in 0..4) { "Invalid number of computers $computer"}
        assert(clients in 0..8) { "Invalid number of clients $clients"}
        assert(clientForPlayer.size == 4) { "Invalid player size ${clientForPlayer.size}"}
        assert(clientNames.size == 8) { "Invalid clientNames size ${clientNames.size}"}
        assert(stoneNumbers.size == Shape.COUNT) { "Invalid stoneNumbers size ${stoneNumbers.size}"}
    }

    override fun write(buffer: ByteBuffer) {
        super.write(buffer)
        buffer.put(player.toByte())
        buffer.put(computer.toByte())
        buffer.put(clients.toByte())
        buffer.put(width.toByte())
        buffer.put(height.toByte())
        // legacy stone numbers
        buffer.put(1, 1, 1, 1, 1)
        buffer.put(gameMode.ordinal.toByte())
        clientForPlayer.forEach { buffer.put(it?.toByte() ?: -1) }
        clientNames.forEach {  name ->
            val bytes = name?.toByteArray() ?: ByteArray(0)
            buffer.put(bytes, 16)
        }
        buffer.put(version.toByte())
        buffer.put(minVersion.toByte())
        stoneNumbers.forEach { buffer.put(it.toByte()) }
    }

    /**
     * Note, we only support minimum version 3 as of 1.1.6
     */
    fun isAtLeastVersion(version: Int): Boolean {
        if (version <= 3) return true
        return this.version >= version
    }

    // TODO: make this nullable
    fun getClient(player: Int) = clientForPlayer[player] ?: -1

    fun isClient(player: Int) = getClient(player) != -1

    fun isComputer(player: Int) = !isClient(player)

    /**
     * @return the name of the client of null if unset
     */
    fun getClientName(client: Int): String? {
        if (client < 0) return null
        return clientNames[client]
    }

    /**
     * @return the name of the client playing the given player, or null if unset
     */
    fun getPlayerName(player: Int): String? {
        if (player < 0) return null
        val client = clientForPlayer[player] ?: return null
        return clientNames[client]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageServerStatus

        if (player != other.player) return false
        if (computer != other.computer) return false
        if (clients != other.clients) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (gameMode != other.gameMode) return false
        if (!clientForPlayer.contentEquals(other.clientForPlayer)) return false
        if (!clientNames.contentEquals(other.clientNames)) return false
        if (version != other.version) return false
        if (minVersion != other.minVersion) return false
        if (!stoneNumbers.contentEquals(other.stoneNumbers)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player
        result = 31 * result + computer
        result = 31 * result + clients
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + gameMode.hashCode()
        result = 31 * result + clientForPlayer.contentHashCode()
        result = 31 * result + clientNames.contentHashCode()
        result = 31 * result + version
        result = 31 * result + minVersion
        result = 31 * result + stoneNumbers.contentHashCode()
        return result
    }

    companion object {
        // highest version we understand.
        private const val VERSION_MAX = 3

        // the original header size, 11 bytes
        private const val HEADER_SIZE_1_0 = 6 + 5

        // the size of version 2 header in bytes, 143 bytes
        private const val HEADER_SIZE_1_5 = HEADER_SIZE_1_0 + 4 + 8 * 16

        // the size of version 3 header in bytes, 166 bytes
        private const val HEADER_SIZE_1_6 = HEADER_SIZE_1_5 + 2 + 21

        fun from(buffer: ByteBuffer): MessageServerStatus {
            // we only support header version 3 in the Android version
            assert(buffer.remaining() >= HEADER_SIZE_1_6) { "Message too small, expected 166 bytes but got ${buffer.remaining()}" }

            // original data
            val player = buffer.get().toInt()
            val computer = buffer.get().toInt()
            val clients = buffer.get().toInt()
            val width = buffer.get().toInt()
            val height = buffer.get().toInt()
            // consume deprecated stoneNumbers
            Array(5) { buffer.get() }
            val gameMode = GameMode.from(buffer.get().toInt())

            // start of 1.5 data
            val clientForPlayer = Array(4) {
                buffer.get().toInt().takeIf { it >= 0 }
            }
            val clientNames = Array(8) {
                val bytes = buffer.getArray(16)
                val length = bytes.indexOfFirst { it == 0.toByte() }

                if (length <= 0) null
                else String(bytes, 0, length, Charsets.UTF_8)
            }

            // start of 1.6 data
            val version = buffer.get().toInt()
            val minVersion = buffer.get().toInt()
            val stoneNumbers = IntArray(Shape.COUNT) { buffer.get().toInt() }

            assert(minVersion <= VERSION_MAX) { "Unsupported version $minVersion" }

            return MessageServerStatus(
                player = player,
                computer = computer,
                clients = clients,
                width = width,
                height = height,
                gameMode = gameMode,
                clientForPlayer = clientForPlayer,
                clientNames = clientNames,
                version = version,
                minVersion = minVersion,
                stoneNumbers = stoneNumbers
            )
        }
    }
}