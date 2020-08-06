package de.saschahlusiak.freebloks.game.lobby

import androidx.annotation.UiThread

interface LobbyDialogDelegate {
    /**
     * The lobby dialog has been cancelled with the back button or touch outside
     */
    @UiThread
    fun onLobbyDialogCancelled()
}