package de.saschahlusiak.freebloks.game

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.about.AboutFragment
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.support.SupportFragment
import de.saschahlusiak.freebloks.game.mainmenu.MainMenuContent
import de.saschahlusiak.freebloks.game.multiplayer.MultiplayerFragment
import de.saschahlusiak.freebloks.game.newgame.NewGameFragment
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.rules.RulesActivity
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuFragment : DialogFragment() {
    private val activity get() = requireActivity() as FreebloksActivity

    private var appIconIsSupport = false

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    @Inject
    lateinit var prefs: Preferences

    override fun getTheme() = R.style.Theme_Freebloks_Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The FreebloksActivity/RateDialog gets to increment it before this is created
        val starts = prefs.numberOfStarts

        appIconIsSupport = !Global.IS_VIP && (starts % Global.SUPPORT_STARTS == 0L)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view as ComposeView
        dialog?.window?.setBackgroundDrawable(null)
        view.setContent {
            Content()

            BackHandler {
                activity.finish()
            }
        }
    }

    @Composable
    private fun Content() {
        AppTheme {
            val soundOn by viewModel.soundsEnabled.collectAsState()
            val connectionStatus by viewModel.connectionStatus.collectAsState()
            val canResume by remember {
                derivedStateOf {
                    (connectionStatus == ConnectionStatus.Connected) && (viewModel.client?.game?.isFinished == false) && (viewModel.client?.game?.isStarted == true)
                }
            }

            val title = stringResource(id = if (appIconIsSupport) R.string.prefs_support else R.string.app_name)

            MainMenuContent(
                soundOn = soundOn,
                canResume = canResume,
                title = title,
                titleOutlined = appIconIsSupport,
                onNewGame = ::onNewGame,
                onTitleClick = ::onAbout,
                onResumeGame = ::onResumeGame,
                onMultiplayer = ::onMultiplayer,
                onSettings = ::onSettings,
                onHelp = ::onHelp,
                onToggleSound = ::onToggleSound
            )
        }
    }

    private fun onNewGame() {
        NewGameFragment().show(parentFragmentManager, null)
    }

    private fun onResumeGame() {
        dismiss()
    }

    private fun onSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        requireContext().startActivity(intent)
    }

    private fun onAbout() {
        if (appIconIsSupport) {
            SupportFragment().show(parentFragmentManager, null)
        } else {
            AboutFragment().show(parentFragmentManager, null)
        }
    }

    private fun onMultiplayer() {
        MultiplayerFragment().show(parentFragmentManager, null)
    }

    private fun onHelp() {
        val intent = Intent(context, RulesActivity::class.java)
        requireContext().startActivity(intent)
    }

    private fun onToggleSound() {
        val soundOn = viewModel.toggleSound()
        Toast.makeText(
            requireContext(),
            if (soundOn) R.string.sound_on else R.string.sound_off,
            Toast.LENGTH_SHORT
        ).show()
    }
}