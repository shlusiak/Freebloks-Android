package de.saschahlusiak.freebloks.game

import android.bluetooth.BluetoothSocket
import de.saschahlusiak.freebloks.model.GameConfig

interface OnStartCustomGameListener {
    @Deprecated("Move to GameConfig")
    fun setClientName(name: String)

    /**
     * Start a new game with the given config.
     */
    fun onStartClientGameWithConfig(config: GameConfig)

    /**
     * Join a game with an already connected BluetoothSocket (client).
     *
     * @param socket a client socket connected to a remote server
     */
    fun onJoinGameWithSocket(socket: BluetoothSocket)

    /**
     * Start hosting a game with an already connected Bluetooth Socket (server),
     * for easy connect from within [JoinDialog].
     *
     * @param socket an incoming connection to our bluetooth server. We need to start a game and start the bridge.
     */
    @Deprecated("move to onStartClientGameWithConfig")
    fun onHostBluetoothGameWithClientSocket(socket: BluetoothSocket)
}
