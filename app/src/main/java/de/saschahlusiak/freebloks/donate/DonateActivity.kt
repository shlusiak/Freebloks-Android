package de.saschahlusiak.freebloks.donate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import kotlinx.android.synthetic.main.donate_activity.*

class DonateActivity : AppCompatActivity(R.layout.donate_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.logEvent("show_donate", null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (Global.IS_FDROID) {
            // F-Droid has no VIP version
            donation_paypal.isChecked = true
            donation_freebloksvip.isVisible = false
        } else {
            donation_freebloksvip.isChecked = true
        }

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