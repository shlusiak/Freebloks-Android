package de.saschahlusiak.freebloks.app

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.utils.PreferenceDelegate
import de.saschahlusiak.freebloks.view.scene.AnimationType
import javax.inject.Inject

class Preferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    internal val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var gameMode: GameMode
        get() = GameMode.from(
            prefs.getInt(
                "gamemode",
                GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal
            )
        )
        set(value) = prefs.edit().putInt("gamemode", value.ordinal).apply()

    var fieldSize by intPreference("fieldsize", default = GameMode.GAMEMODE_4_COLORS_4_PLAYERS.defaultBoardSize())

    var difficulty by intPreference("difficulty", default = GameConfig.DEFAULT_DIFFICULTY)

    var playerName by stringPreference("player_name")

    var serverAddress by stringPreference("custom_server")

    var rateShowAgain by booleanPreference("rate_show_again", default = true)

    var firstStarted by longPreference("firstStarted")

    var numberOfStarts by longPreference("rate_number_of_starts")

    var viewScale by floatPreference("view_scale", default = 1f)

    val theme by stringPreference("theme", default = "texture_wood")

    val boardTheme by stringPreference("board_theme", default = "field_wood")

    val autoResume by booleanPreference("auto_resume")

    var sounds by booleanPreference("sounds", default = true)

    val vibrationEnabled by booleanPreference("vibrate", default = true)

    val showSeeds by booleanPreference("show_seeds", default = true)

    val showOpponents by booleanPreference("show_opponents", default = true)

    private val animationType by stringPreference("animations", AnimationType.Full.settingsValue)

    val showAnimations
        get() = AnimationType.entries.firstOrNull { it.settingsValue == animationType } ?: AnimationType.Full

    val snapAid by booleanPreference("snap_aid", default = true)

    val skipIntro by booleanPreference("skip_intro")
}

private fun Preferences.stringPreference(name: String, default: String = "") = PreferenceDelegate(prefs, name, default)
private fun Preferences.intPreference(name: String, default: Int = 0) = PreferenceDelegate(prefs, name, default)
private fun Preferences.longPreference(name: String, default: Long = 0L) = PreferenceDelegate(prefs, name, default)
private fun Preferences.floatPreference(name: String, default: Float = 0f) = PreferenceDelegate(prefs, name, default)
private fun Preferences.booleanPreference(name: String, default: Boolean = false) =
    PreferenceDelegate(prefs, name, default)