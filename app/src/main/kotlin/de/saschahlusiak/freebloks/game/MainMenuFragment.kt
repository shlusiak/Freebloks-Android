package de.saschahlusiak.freebloks.game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.about.AboutFragment
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.databinding.MainMenuFragmentBinding
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.game.newgame.NewGameFragment
import de.saschahlusiak.freebloks.game.dialogs.CustomGameFragment
import de.saschahlusiak.freebloks.game.multiplayer.MultiplayerFragment
import de.saschahlusiak.freebloks.game.mainmenu.MainMenuContent
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.rules.RulesActivity
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuFragment : MaterialDialogFragment(R.layout.main_menu_fragment), OnLongClickListener {
    private val activity get() = requireActivity() as FreebloksActivity
    private var appIconIsDonate = false

    private val binding by viewBinding(MainMenuFragmentBinding::bind)
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    @Inject
    lateinit var analytics: AnalyticsProvider

    override fun getTheme() = R.style.Theme_Freebloks_Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val starts = prefs.getLong("rate_number_of_starts", 0)

        appIconIsDonate = !Global.IS_VIP && (starts % Global.DONATE_STARTS == 0L)
        if (appIconIsDonate) {
            analytics.logEvent("menu_show_donate", null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (Feature.COMPOSE)
            ComposeView(requireContext())
        else super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (view is ComposeView) {
            dialog?.window?.setBackgroundDrawable(null)
            view.setContent {
                Content()
            }
            return
        } else with(binding) {
            newGame.setOnClickListener { onNewGame() }
            resumeGame.setOnClickListener { onResumeGame() }
            settings.setOnClickListener { onSettings() }
            multiplayer.setOnClickListener { onMultiplayer() }
            rules.setOnClickListener { onHelp() }
            newGameCustom.setOnClickListener { onCustomGame() }

            if (appIconIsDonate) {
                appIcon.setImageResource(R.drawable.ic_coffee)
            }
            appIcon.setOnClickListener { onAbout() }

            soundToggleButton.setOnClickListener { onToggleSound() }

            viewModel.soundsEnabledLiveData.observe(viewLifecycleOwner) { enabled ->
                val res = if (enabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off
                soundToggleButton.setIconResource(res)
            }

            viewModel.connectionStatus.asLiveData().observe(viewLifecycleOwner) {
                val canResume =
                    (it == ConnectionStatus.Connected) && (viewModel.client?.game?.isFinished == false) && (viewModel.client?.game?.isStarted == true)

                resumeGame.isEnabled = canResume
                dialog?.setCanceledOnTouchOutside(canResume)
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

    override fun onCreateDialog(savedInstanceState: Bundle?): MaterialDialog {
        return object : MaterialDialog(requireContext(), theme, apply = !Feature.COMPOSE) {
            override fun onBackPressed() {
                activity.finish()
            }
        }.apply {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onStart() {
        super.onStart()

        if (appIconIsDonate && !Feature.COMPOSE) {
            binding.appIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.donate_pulse))
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

            val intent = Intent(context, DonateActivity::class.java)
            requireContext().startActivity(intent)
        } else {
            analytics.logEvent("menu_about_click", null)

            AboutFragment().show(parentFragmentManager, null)
        }
    }

    private fun onMultiplayer() {
        analytics.logEvent("menu_multiplayer_click", null)
        MultiplayerFragment().show(parentFragmentManager, null)
    }

    private fun onCustomGame() {
        analytics.logEvent("menu_custom_game_click", null)
        CustomGameFragment().show(parentFragmentManager, null)
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
        )
            .show()
    }

    override fun onLongClick(v: View): Boolean {
        return when (v.id) {
            R.id.new_game -> {
                activity.startNewDefaultGame()
                dismiss()
                true
            }

            else -> false
        }
    }
}