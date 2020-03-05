package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.AboutActivity
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.game.dialogs.CustomGameDialog
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences
import de.saschahlusiak.freebloks.rules.RulesActivity
import kotlinx.android.synthetic.main.game_menu_dialog.view.*

class MainMenuDialogFragment : DialogFragment(), View.OnClickListener, OnLongClickListener {
    private val activity get() = requireActivity() as FreebloksActivity
    private val analytics by lazy { FirebaseAnalytics.getInstance(requireContext()) }
    private var appIconIsDonate = false
    private lateinit var appIcon: ImageView

    private val viewModel by lazy { activity.viewModel }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.game_menu_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val starts = prefs.getLong("rate_number_of_starts", 0)

        appIconIsDonate = !Global.IS_VIP && starts % Global.DONATE_STARTS == 0L

        view.findViewById<View>(R.id.new_game).setOnClickListener(this)
        view.findViewById<View>(R.id.new_game).setOnLongClickListener(this)
        view.findViewById<View>(R.id.resume_game).setOnClickListener(this)
        view.findViewById<View>(R.id.preferences).setOnClickListener(this)
        view.findViewById<View>(R.id.join_game).setOnClickListener(this)
        view.findViewById<View>(R.id.rules).setOnClickListener(this)
        view.findViewById<View>(R.id.new_game_custom).setOnClickListener(this)

        appIcon = view.findViewById(R.id.appIcon)

        if (appIconIsDonate) {
            analytics.logEvent("show_donate_button", null)
            appIcon.setImageResource(R.drawable.ic_action_favorite)
        }
        appIcon.setOnClickListener(this)

        view.sound_toggle_button.setOnClickListener { viewModel.soundsEnabled = !viewModel.soundsEnabled }

        viewModel.soundsEnabledLiveData.observe(viewLifecycleOwner, Observer { enabled ->
            val res = if (enabled) R.drawable.ic_volume_up_white_48dp else R.drawable.ic_volume_off_white_48dp
            view.sound_toggle_button.setImageResource(res)
        })

        viewModel.connectionStatus.observe(viewLifecycleOwner, Observer {
            val canResume = (it == ConnectionStatus.Connected) && (viewModel.client?.game?.isFinished == false)

            view.resume_game.isEnabled = canResume
            dialog?.setCanceledOnTouchOutside(canResume)
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object: Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                ownerActivity?.finish()
            }
        }.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onStart() {
        super.onStart()
        if (appIconIsDonate) {
            appIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.heart))
        }
    }

    override fun onClick(v: View) {
        val intent: Intent
        when (v.id) {
            R.id.new_game -> {
                dismiss()
                activity.showDialog(FreebloksActivity.DIALOG_SINGLE_PLAYER)
            }
            R.id.resume_game -> dismiss()
            R.id.preferences -> {
                intent = Intent(context, FreebloksPreferences::class.java)
                requireContext().startActivity(intent)
            }
            R.id.appIcon -> {
                intent = Intent(context, if (appIconIsDonate) DonateActivity::class.java else AboutActivity::class.java)
                requireContext().startActivity(intent)
            }
            R.id.join_game -> activity.showDialog(FreebloksActivity.DIALOG_JOIN)
            R.id.new_game_custom -> CustomGameDialog().show(activity.supportFragmentManager, null)
            R.id.rules -> {
                intent = Intent(context, RulesActivity::class.java)
                requireContext().startActivity(intent)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.new_game -> {
                activity.startNewGame()
                dismiss()
                return true
            }
            else -> return false
        }
    }
}