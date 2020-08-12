package de.saschahlusiak.freebloks.game

import android.bluetooth.BluetoothDevice
import de.saschahlusiak.freebloks.model.GameConfig
import kotlinx.coroutines.Job

/**
 * The interface implemented by [FreebloksActivity], to be used by dialogs.
 */
interface OnStartCustomGameListener {
    /**
     * Start a new game with the given config.
     *
     * @param config the game config
     * @param localClientName the initial local client name for all players that are requested
     */
    fun onStartClientGameWithConfig(config: GameConfig, localClientName: String?): Job

    /**
     * Join a game by connecting to the given remote device.
     *
     * @param config the game config, usually just showLobby = true
     * @param localClientName the initial local client name
     * @param device the remote device
     */
    fun onConnectToBluetoothDevice(config: GameConfig, localClientName: String?, device: BluetoothDevice)

    /**
     * Show the [MainMenuFragment], no questions asked.
     */
    fun showMainMenu()

    /**
     * Start a new game with the last config or a default config
     */
    fun startNewDefaultGame()
}


