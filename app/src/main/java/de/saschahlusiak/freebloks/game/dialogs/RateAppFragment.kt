package de.saschahlusiak.freebloks.game.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import de.saschahlusiak.freebloks.databinding.RateAppFragmentBinding
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.prefs
import de.saschahlusiak.freebloks.utils.viewBinding

class RateAppFragment : MaterialDialogFragment(R.layout.rate_app_fragment) {

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    private val binding by viewBinding(RateAppFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setTitle(R.string.rate_freebloks_title)
        analytics.logEvent("rate_show", null)

        with(binding) {
            ok.setOnClickListener {
                analytics.logEvent("rate_yes_click", null)
                val intent = Intent(
                    "android.intent.action.VIEW",
                    Uri.parse(Global.getMarketURLString(BuildConfig.APPLICATION_ID))
                )
                prefs.edit()
                    .putBoolean("rate_show_again", false)
                    .apply()

                dismiss()
                startActivity(intent)
            }

            later.setOnClickListener {
                analytics.logEvent("rate_later_click", null)
                dismiss()
            }

            no.setOnClickListener {
                analytics.logEvent("rate_no_click", null)
                prefs.edit()
                    .putBoolean("rate_show_again", false)
                    .apply()

                dismiss()
            }

            link.setOnClickListener {
                analytics.logEvent("rate_donate_click", null)
                startActivity(Intent(context, DonateActivity::class.java))
            }
        }
    }

    companion object {
        private val tag = RateAppFragment::class.java.simpleName

        fun shouldShowRateDialog(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()

            var starts = prefs.getLong("rate_number_of_starts", 0) + 1
            var firstStarted = prefs.getLong("rate_first_started", 0)

            var show = false
            if (prefs.getBoolean("rate_show_again", true)) {
                if (firstStarted <= 0) {
                    firstStarted = System.currentTimeMillis()
                }
                Log.d(tag, "started $starts times")
                Log.d(tag, "elapsed time since first start: " + (System.currentTimeMillis() - firstStarted))
                if (starts >= Global.RATE_MIN_STARTS) starts = Global.RATE_MIN_STARTS.toLong()
                if (starts >= Global.RATE_MIN_STARTS && System.currentTimeMillis() - firstStarted >= Global.RATE_MIN_ELAPSED) {
                    starts = 0
                    firstStarted = System.currentTimeMillis()
                    show = true
                }
                editor.putLong("rate_first_started", firstStarted)
            }

            editor.putLong("rate_number_of_starts", starts)
            editor.apply()

            return show
        }
    }
}