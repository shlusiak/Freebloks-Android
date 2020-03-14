package de.saschahlusiak.freebloks.preferences

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper

class SettingsActivityViewModel(val context: Application): AndroidViewModel(context) {
    val googleHelper = GooglePlayGamesHelper(context)

    val googleAccount = googleHelper.googleAccount
    val currentPlayer = googleHelper.currentPlayer
}