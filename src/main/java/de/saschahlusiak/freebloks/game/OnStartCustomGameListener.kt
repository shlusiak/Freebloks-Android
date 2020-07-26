package de.saschahlusiak.freebloks.game

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import de.saschahlusiak.freebloks.model.GameConfig

/**
 * The interface implemented by [FreebloksActivity], to be used by dialogs.
 */
interface OnStartCustomGameListener {
    /**
     * Start a new game with the given config.
     *
     * @param config the game config
     * @param localClientName the initial local client name for all players that are requested
     * @param runAfter an optional Runnable to run after connection is successful
     */
    fun onStartClientGameWithConfig(config: GameConfig, localClientName: String?, runAfter: () -> Unit = {})

    /**
     * Join a game by connecting to the given remote device.
     *
     * @param config the game config, usually just showLobby = true
     * @param localClientName the initial local client name
     * @param device the remote device
     */
    fun onConnectToBluetoothDevice(config: GameConfig, localClientName: String?, device: BluetoothDevice)

    /**
     * Show the [MainMenu], no questions asked.
     */
    fun showMainMenu()

    /**
     * Start a new game with the last config or a default config
     */
    fun startNewDefaultGame()
}


