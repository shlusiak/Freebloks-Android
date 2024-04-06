package de.saschahlusiak.freebloks.game.newgame

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.defaultBoardSize
import javax.inject.Inject

@AndroidEntryPoint
class NewGameFragment : DialogFragment() {
    private val listener get() = (requireActivity() as OnStartCustomGameListener)

    @Inject
    lateinit var prefs: SharedPreferences

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val previousGameMode = from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal))
        val previousSize = prefs.getInt("fieldsize", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.defaultBoardSize())
        val difficulty = prefs.getInt("difficulty", GameConfig.DEFAULT_DIFFICULTY)

        view as ComposeView
        dialog?.window?.setBackgroundDrawable(null)
        view.setContent {
            Content(previousGameMode, previousSize, difficulty)
        }
    }

    @Composable
    private fun Content(gameMode: GameMode, size: Int, difficulty: Int) {
        AppTheme {
            NewGameScreen(gameMode, size, difficulty) { config ->
                listener.onStartClientGameWithConfig(config, null)
                dismiss()

                prefs.edit()
                    .putInt("gamemode", config.gameMode.ordinal)
                    .putInt("fieldsize", config.fieldSize)
                    .putInt("difficulty", config.difficulty)
                    .apply()
            }
        }
    }
}