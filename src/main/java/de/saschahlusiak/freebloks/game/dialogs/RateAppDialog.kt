package de.saschahlusiak.freebloks.game.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.prefs
import kotlinx.android.synthetic.main.rate_app_dialog.*

class RateAppDialog : MaterialDialogFragment() {

    override fun getTheme() = R.style.Theme_Freebloks_Light_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.rate_app_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setTitle(R.string.rate_freebloks_title)
        analytics.logEvent("show_rate", null)

        ok.setOnClickListener {
            val intent = Intent("android.intent.action.VIEW", Uri.parse(Global.getMarketURLString(BuildConfig.APPLICATION_ID)))
            prefs.edit()
                .putBoolean("rate_show_again", false)
                .apply()

            dismiss()
            startActivity(intent)
        }

        later.setOnClickListener { dismiss() }

        no.setOnClickListener {
            prefs.edit()
                .putBoolean("rate_show_again", false)
                .apply()

            dismiss()
        }

        link.setOnClickListener {
            startActivity(Intent(context, DonateActivity::class.java))
        }
    }

    companion object {
        private val tag = RateAppDialog::class.java.simpleName

        fun checkShowRateDialog(context: Context?): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()

            var starts = prefs.getLong("rate_number_of_starts", 0)
            var firstStarted = prefs.getLong("rate_first_started", 0)

            var show = false
            starts++
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