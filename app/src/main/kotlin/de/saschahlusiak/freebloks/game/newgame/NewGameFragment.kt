package de.saschahlusiak.freebloks.game.newgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameMode
import javax.inject.Inject

@AndroidEntryPoint
class NewGameFragment : DialogFragment() {
    private val listener get() = (requireActivity() as OnStartCustomGameListener)

    @Inject
    lateinit var prefs: Preferences

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val previousGameMode = prefs.gameMode
        val previousSize = prefs.fieldSize
        val difficulty = prefs.difficulty

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

                prefs.gameMode = config.gameMode
                prefs.fieldSize = config.fieldSize
                prefs.difficulty = config.difficulty
            }
        }
    }
}