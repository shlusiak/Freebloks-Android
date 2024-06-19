package de.saschahlusiak.freebloks.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.database.HighScoreDatabase
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    val googleHelper: GooglePlayGamesHelper,
    private val highScoreDatabase: HighScoreDatabase
): ViewModel() {
    val isSignedIn = googleHelper.signedIn
    val playerName = googleHelper.playerName

    val canClearStatistics = highScoreDatabase.getAllAsFlow(null).map { it.isNotEmpty() }

    fun clearStatistics() {
        viewModelScope.launch {
            highScoreDatabase.clear()
        }
    }
}