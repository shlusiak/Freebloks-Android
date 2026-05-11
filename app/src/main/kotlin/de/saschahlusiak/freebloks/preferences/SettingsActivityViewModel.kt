package de.saschahlusiak.freebloks.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.database.HighScoreDatabase
import de.saschahlusiak.freebloks.theme.ColorTheme
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.ThemeManager
import de.saschahlusiak.freebloks.util.AnimationType
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    val googleHelper: GooglePlayGamesHelper,
    private val prefs: Preferences,
    private val themeManager: ThemeManager
): ViewModel() {
    val isSignedIn = googleHelper.signedIn
    val playerName = googleHelper.playerName

    val themes = themeManager.backgroundThemes
    val boardThemes = themeManager.boardThemes

    val sounds = MutableStateFlow(prefs.sounds)
    val vibrate = MutableStateFlow(prefs.vibrationEnabled)
    val autoResume = MutableStateFlow(prefs.autoResume)
    val skipIntro = MutableStateFlow(prefs.skipIntro)
    val snap = MutableStateFlow(prefs.snapAid)
    val seeds = MutableStateFlow(prefs.showSeeds)
    val opponents = MutableStateFlow(prefs.showOpponents)
    val animations = MutableStateFlow(prefs.showAnimations)
    val theme = MutableStateFlow(themeManager.getTheme(prefs.theme, ColorThemes.Blue))
    val boardTheme = MutableStateFlow(themeManager.getTheme(prefs.boardTheme, ColorThemes.Blue))

    fun setSounds(enabled: Boolean) {
        prefs.sounds = enabled
        sounds.value = enabled
    }

    fun setVibrate(enabled: Boolean) {
        prefs.vibrationEnabled = enabled
        vibrate.value = enabled
    }

    fun setSnap(enabled: Boolean) {
        prefs.snapAid = enabled
        snap.value = enabled
    }

    fun setSeeds(enabled: Boolean) {
        prefs.showSeeds = enabled
        seeds.value = enabled
    }

    fun setOpponents(enabled: Boolean) {
        prefs.showOpponents = enabled
        opponents.value = enabled
    }

    fun setAnimations(value: AnimationType) {
        prefs.showAnimations = value
        animations.value = value
    }

    fun setSkipIntro(enabled: Boolean) {
        prefs.skipIntro = enabled
        skipIntro.value = enabled
    }

    fun setAutoResume(enabled: Boolean) {
        prefs.autoResume = enabled
        autoResume.value = enabled
    }
}