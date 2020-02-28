package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnLongClickListener
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.AboutActivity
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences
import kotlinx.android.synthetic.main.game_menu_dialog.*

@Deprecated("Convert to DialogFragment")
class GameMenu(context: Context) : Dialog(context), View.OnClickListener, OnLongClickListener {
    private val activity get() = ownerActivity as FreebloksActivity
    private val appIconIsDonate: Boolean
    private lateinit var appIcon: ImageView

    private val viewModel by lazy { activity.viewModel }

    init {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val starts = prefs.getLong("rate_number_of_starts", 0)
        appIconIsDonate = !Global.IS_VIP && starts % Global.DONATE_STARTS == 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.game_menu_dialog)

        findViewById<View>(R.id.new_game).setOnClickListener(this)
        findViewById<View>(R.id.new_game).setOnLongClickListener(this)
        findViewById<View>(R.id.resume_game).setOnClickListener(this)
        findViewById<View>(R.id.preferences).setOnClickListener(this)
        findViewById<View>(R.id.join_game).setOnClickListener(this)
        findViewById<View>(R.id.rules).setOnClickListener(this)
        findViewById<View>(R.id.new_game_custom).setOnClickListener(this)

        appIcon = findViewById(R.id.appIcon)

        if (appIconIsDonate) {
            FirebaseAnalytics.getInstance(context).logEvent("show_donate_button", null)
            appIcon.setImageResource(R.drawable.ic_action_favorite)
        }
        appIcon.setOnClickListener(this)

        sound_toggle_button.setOnClickListener {
            viewModel.soundsEnabled = !viewModel.soundsEnabled
            onSoundChanged(viewModel.soundsEnabled)
        }
    }

    fun setResumeEnabled(canResume: Boolean) {
        findViewById<View>(R.id.resume_game).isEnabled = canResume
        setCanceledOnTouchOutside(canResume)
    }

    override fun onStart() {
        super.onStart()
        if (appIconIsDonate) {
            appIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.heart))
        }
    }

    private fun onSoundChanged(enabled: Boolean) {
        sound_toggle_button.setImageResource(if (enabled) R.drawable.ic_volume_up_white_48dp else R.drawable.ic_volume_off_white_48dp)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        onSoundChanged(viewModel.soundsEnabled)
    }

    override fun onBackPressed() {
        ownerActivity?.finish()
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
                context.startActivity(intent)
            }
            R.id.appIcon -> {
                intent = Intent(context, if (appIconIsDonate) DonateActivity::class.java else AboutActivity::class.java)
                context.startActivity(intent)
            }
            R.id.join_game -> activity.showDialog(FreebloksActivity.DIALOG_JOIN)
            R.id.new_game_custom -> activity.showDialog(FreebloksActivity.DIALOG_CUSTOM_GAME)
            R.id.rules -> {
                intent = Intent(context, RulesActivity::class.java)
                context.startActivity(intent)
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