package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.donate.DonateActivity
import kotlinx.android.synthetic.main.rate_app_dialog.*

class RateAppDialog(context: Context) : Dialog(context, R.style.Theme_Freebloks_Light_Dialog) {

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.rate_app_dialog)

        setTitle(R.string.rate_freebloks_title)
        FirebaseAnalytics.getInstance(context).logEvent("show_rate", null)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        ok.setOnClickListener {
            val intent = Intent("android.intent.action.VIEW", Uri.parse(Global.getMarketURLString(context.packageName)))
            prefs.edit()
                .putBoolean("rate_show_again", false)
                .apply()

            dismiss()
            context.startActivity(intent)
        }

        later.setOnClickListener { dismiss() }

        no.setOnClickListener {
            prefs.edit()
                .putBoolean("rate_show_again", false)
                .apply()

            dismiss()
        }

        link.setOnClickListener {
            context.startActivity(Intent(context, DonateActivity::class.java))
        }
    }

    companion object {
        private val tag = RateAppDialog::class.java.simpleName

        @JvmStatic
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