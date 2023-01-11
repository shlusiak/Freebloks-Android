package de.saschahlusiak.freebloks.preferences

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    val googleHelper: GooglePlayGamesHelper
): ViewModel() {
    val isSignedIn = googleHelper.signedIn
    val playerName = googleHelper.playerName
}