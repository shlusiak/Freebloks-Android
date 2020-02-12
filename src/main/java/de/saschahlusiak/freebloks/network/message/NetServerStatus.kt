package de.saschahlusiak.freebloks.network.message

import android.content.res.Resources
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.controller.GameMode
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.network.Message
import de.saschahlusiak.freebloks.network.MessageType
import de.saschahlusiak.freebloks.network.put
import de.saschahlusiak.freebloks.utils.put
import de.saschahlusiak.freebloks.utils.toUnsignedByte
import java.io.Serializable
import java.nio.ByteBuffer
import java.security.InvalidParameterException

data class NetServerStatus(
    val player: Int,                // int8
    val computer: Int,              // int8
    val clients: Int,               // int8
    val width: Int,                 // int8
    val height: Int,                // int8
//    val _stoneNumbers: IntArray,  //int8[5]
    val gameMode: GameMode,         // int8

// since 1.5, optional
    val spieler: IntArray,          // int8[4]
    val clientNames: Array<String?>, // uint8[8][16]
    val version: Int,               // int8
    val minVersion: Int,            // int8
    val stoneNumbers: IntArray      // int8[21]

) : Message(MessageType.ServerStatus, HEADER_SIZE_1_5), Serializable {

    init {
        assert(player in 0..4) { "Invalid number of players $player"}
        assert(computer in 0..4) { "Invalid number of computers $computer"}
        assert(clients in 0..8) { "Invalid number of clients $clients"}
        assert(spieler.size == 4) { "Invalid spieler size ${spieler.size}"}
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
        buffer.put(1, 1, 1, 1, 1)
        buffer.put(gameMode.ordinal.toByte())
        spieler.forEach { buffer.put(it.toByte()) }
        clientNames.forEach { name ->
            val name = name ?: ""
            val padding = 16 - name.length

            name.forEach { buffer.put(it.toByte()) }
            repeat(padding) { buffer.put(0) }
        }
        buffer.put(version.toByte())
        buffer.put(minVersion.toByte())
        stoneNumbers.forEach { buffer.put(it.toByte()) }
    }

    fun isVersion(version: Int): Boolean {
        return this.version >= version
    }

    fun getClientName(resources: Resources?, client: Int): String {
        val default = resources?.getString(R.string.client_d, client + 1) ?: "Client $client"
        if (client < 0) return default
        return clientNames[client] ?: default
    }

    fun getPlayerName(resources: Resources, player: Int, color: Int): String {
        if (player < 0) throw InvalidParameterException()
        val colorName = resources.getStringArray(R.array.color_names)[color]
        if (spieler[player] < 0) return colorName
        return clientNames[spieler[player]] ?: colorName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetServerStatus

        if (player != other.player) return false
        if (computer != other.computer) return false
        if (clients != other.clients) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (gameMode != other.gameMode) return false
        if (!spieler.contentEquals(other.spieler)) return false
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
        result = 31 * result + spieler.contentHashCode()
        result = 31 * result + clientNames.contentHashCode()
        result = 31 * result + version
        result = 31 * result + minVersion
        result = 31 * result + stoneNumbers.contentHashCode()
        return result
    }

    companion object {
        // highest version we understand.
        private const val VERSION_MAX = 3

        // the size of version 1.5 header in bytes
        const val HEADER_SIZE_1_5 = 166

        fun from(buffer: ByteBuffer): NetServerStatus {
            assert(buffer.remaining() >= HEADER_SIZE_1_5) { "Unsupported header data size ${buffer.remaining()}" }

            val player = buffer.get().toInt()
            val computer = buffer.get().toInt()
            val clients = buffer.get().toInt()
            val width = buffer.get().toInt()
            val height = buffer.get().toInt()
            // consume deprecated stoneNumbers
            Array(5) { buffer.get() }
            val gamemode = GameMode.from(buffer.get().toInt())
            val spieler = IntArray(4) { buffer.get().toInt() }
            val clientNames = Array<String?>(8) {
                val chars = CharArray(16) { buffer.get().toUnsignedByte().toChar() }
                val length = chars.indexOfFirst { it == 0.toChar() }

                if (length <= 0) null
                else String(chars, 0, length)
            }
            val version = buffer.get().toInt()
            val minVersion = buffer.get().toInt()
            val stoneNumbers = IntArray(Shape.COUNT) { buffer.get().toInt() }

            assert(minVersion <= VERSION_MAX) { "Unsupported version $minVersion" }

            return NetServerStatus(
                player = player,
                computer = computer,
                clients = clients,
                width = width,
                height = height,
                gameMode = gamemode,
                spieler = spieler,
                clientNames = clientNames,
                version = version,
                minVersion = minVersion,
                stoneNumbers = stoneNumbers
            )
        }
    }
}