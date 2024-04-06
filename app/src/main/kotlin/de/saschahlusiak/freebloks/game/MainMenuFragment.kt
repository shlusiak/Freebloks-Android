package de.saschahlusiak.freebloks.game

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.donate.DonateFragment
import de.saschahlusiak.freebloks.game.mainmenu.MainMenuContent
import de.saschahlusiak.freebloks.game.multiplayer.MultiplayerFragment
import de.saschahlusiak.freebloks.game.newgame.NewGameFragment
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.rules.RulesActivity
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuFragment : DialogFragment() {
    private val activity get() = requireActivity() as FreebloksActivity

    private var appIconIsDonate = false

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    @Inject
    lateinit var analytics: AnalyticsProvider

    @Inject
    lateinit var prefs: Preferences

    override fun getTheme() = R.style.Theme_Freebloks_Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The FreebloksActivity/RateDialog gets to increment it before this is created
        val starts = prefs.numberOfStarts

        appIconIsDonate = !Global.IS_VIP && (starts % Global.DONATE_STARTS == 0L)
        if (appIconIsDonate) {
            analytics.logEvent("menu_show_donate", null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view as ComposeView
        dialog?.window?.setBackgroundDrawable(null)
        view.setContent {
            Content()
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

            val title = stringResource(id = if (appIconIsDonate) R.string.prefs_donation else R.string.app_name)

            MainMenuContent(
                soundOn = soundOn,
                canResume = canResume,
                title = title,
                titleOutlined = appIconIsDonate,
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : AppCompatDialog(requireContext(), theme) {
            @Deprecated("Deprecated in Java")
            @SuppressLint("MissingSuperCall")
            override fun onBackPressed() {
                activity.finish()
            }
        }
    }

    private fun onNewGame() {
        analytics.logEvent("menu_new_game_click", null)
        NewGameFragment().show(parentFragmentManager, null)
    }

    private fun onResumeGame() {
        analytics.logEvent("menu_resume_click", null)
        dismiss()
    }

    private fun onSettings() {
        analytics.logEvent("menu_settings_click", null)
        val intent = Intent(context, SettingsActivity::class.java)
        requireContext().startActivity(intent)
    }

    private fun onAbout() {
        if (appIconIsDonate) {
            analytics.logEvent("menu_donate_click", null)

            DonateFragment().show(parentFragmentManager, null)
        } else {
            analytics.logEvent("menu_about_click", null)

            AboutFragment().show(parentFragmentManager, null)
        }
    }

    private fun onMultiplayer() {
        analytics.logEvent("menu_multiplayer_click", null)
        MultiplayerFragment().show(parentFragmentManager, null)
    }

    private fun onHelp() {
        analytics.logEvent("menu_rules_click", null)
        val intent = Intent(context, RulesActivity::class.java)
        requireContext().startActivity(intent)
    }

    private fun onToggleSound() {
        analytics.logEvent("menu_sound_click", null)

        val soundOn = viewModel.toggleSound()
        Toast.makeText(
            requireContext(),
            if (soundOn) R.string.sound_on else R.string.sound_off,
            Toast.LENGTH_SHORT
        ).show()
    }
}