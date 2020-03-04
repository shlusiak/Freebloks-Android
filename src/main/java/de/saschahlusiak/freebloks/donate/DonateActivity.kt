package de.saschahlusiak.freebloks.donate

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import com.google.firebase.analytics.FirebaseAnalytics
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import kotlinx.android.synthetic.main.donate_activity.*

class DonateActivity : Activity() {
    private val analytics by lazy { FirebaseAnalytics.getInstance(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.logEvent("show_donate", null)

        setContentView(R.layout.donate_activity)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        donation_freebloksvip.isChecked = true
        next.setOnClickListener { onNextButtonPress() }
        donate_icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.heart))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onNextButtonPress() {
        when (radioGroup.checkedRadioButtonId) {
            R.id.donation_freebloksvip -> {
                analytics.logEvent("donate_freebloksvip", null)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Global.getMarketURLString("de.saschahlusiak.freebloksvip")))
                startActivity(intent)
            }
            R.id.donation_paypal -> {
                analytics.logEvent("donate_paypal", null)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/saschahlusiak/3eur"))
                startActivity(intent)
            }
        }
        finish()
    }
}