package de.saschahlusiak.freebloks.game

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import de.saschahlusiak.freebloks.model.GameConfig

interface OnStartCustomGameListener {
    @Deprecated("Move to GameConfig")
    fun setClientName(name: String)

    /**
     * Start a new game with the given config.
     */
    fun onStartClientGameWithConfig(config: GameConfig, runAfter: Runnable? = null)

    /**
     * Join a game by connecting to the given remote device.
     *
     * @param device the remote device
     */
    fun onConnectToBluetoothDevice(device: BluetoothDevice)
}
