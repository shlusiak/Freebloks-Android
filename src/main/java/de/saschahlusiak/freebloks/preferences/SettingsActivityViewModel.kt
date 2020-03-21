package de.saschahlusiak.freebloks.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.saschahlusiak.freebloks.DependencyProvider

class SettingsActivityViewModel(val context: Application): AndroidViewModel(context) {
    val googleHelper = DependencyProvider.googlePlayGamesHelper()

    val isSignedIn = googleHelper.signedIn
    val playerName = googleHelper.playerName
}