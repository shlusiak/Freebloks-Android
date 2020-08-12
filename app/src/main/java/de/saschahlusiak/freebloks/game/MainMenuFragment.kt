package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.AboutFragment
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.game.dialogs.ColorListFragment
import de.saschahlusiak.freebloks.game.dialogs.CustomGameFragment
import de.saschahlusiak.freebloks.game.dialogs.MultiplayerFragment
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.rules.RulesActivity
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import kotlinx.android.synthetic.main.main_menu_fragment.*
import kotlinx.android.synthetic.main.main_menu_fragment.view.*

class MainMenuFragment : MaterialDialogFragment(R.layout.main_menu_fragment), View.OnClickListener, OnLongClickListener {
    private val activity get() = requireActivity() as FreebloksActivity
    private var appIconIsDonate = false
    private lateinit var appIcon: ImageView

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    override fun getTheme() = R.style.Theme_Freebloks_Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val starts = prefs.getLong("rate_number_of_starts", 0)

        appIconIsDonate = !Global.IS_VIP && (starts % Global.DONATE_STARTS == 0L)
        if (appIconIsDonate) {
            analytics.logEvent("menu_show_donate", null)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        new_game.setOnClickListener(this)
        new_game.setOnLongClickListener(this)
        resume_game.setOnClickListener(this)
        settings.setOnClickListener(this)
        multiplayer.setOnClickListener(this)
        rules.setOnClickListener(this)
        new_game_custom.setOnClickListener(this)

        appIcon = view.findViewById(R.id.appIcon)

        if (appIconIsDonate) {
            appIcon.setImageResource(R.drawable.ic_coffee)
        }
        appIcon.setOnClickListener(this)

        view.sound_toggle_button.setOnClickListener {
            analytics.logEvent("menu_sound_click", null)

            val soundOn = viewModel.toggleSound()
            Toast.makeText(requireContext(), if (soundOn) R.string.sound_on else R.string.sound_off, Toast.LENGTH_SHORT).show()
        }

        viewModel.soundsEnabledLiveData.observe(viewLifecycleOwner, Observer { enabled ->
            val res = if (enabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off
            view.sound_toggle_button.setImageResource(res)
        })

        viewModel.connectionStatus.observe(viewLifecycleOwner, Observer {
            val canResume = (it == ConnectionStatus.Connected) && (viewModel.client?.game?.isFinished == false) && (viewModel.client?.game?.isStarted == true)

            view.resume_game.isEnabled = canResume
            dialog?.setCanceledOnTouchOutside(canResume)
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object: MaterialDialog(requireContext(), theme) {
            override fun onBackPressed() {
                ownerActivity?.finish()
            }
        }.apply {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onStart() {
        super.onStart()

        if (appIconIsDonate) {
            appIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.donate_pulse))
        }
    }

    override fun onClick(v: View) {
        val intent: Intent
        when (v.id) {
            R.id.new_game -> {
                analytics.logEvent("menu_new_game_click", null)
                ColorListFragment().show(parentFragmentManager, null)
            }
            R.id.resume_game -> {
                analytics.logEvent("menu_resume_click", null)
                dismiss()
            }
            R.id.settings -> {
                analytics.logEvent("menu_settings_click", null)
                intent = Intent(context, SettingsActivity::class.java)
                requireContext().startActivity(intent)
            }
            R.id.appIcon -> {
                if (appIconIsDonate) {
                    analytics.logEvent("menu_donate_click", null)

                    intent = Intent(context, DonateActivity::class.java)
                    requireContext().startActivity(intent)
                } else {
                    analytics.logEvent("menu_about_click", null)

                    AboutFragment().show(parentFragmentManager, null)
                }
            }
            R.id.multiplayer -> {
                analytics.logEvent("menu_multiplayer_click", null)
                MultiplayerFragment().show(parentFragmentManager, null)
            }
            R.id.new_game_custom -> {
                analytics.logEvent("menu_custom_game_click", null)
                CustomGameFragment().show(parentFragmentManager, null)
            }
            R.id.rules -> {
                analytics.logEvent("menu_rules_click", null)
                intent = Intent(context, RulesActivity::class.java)
                requireContext().startActivity(intent)
            }
        }
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