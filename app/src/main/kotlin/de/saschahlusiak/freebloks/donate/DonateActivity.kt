package de.saschahlusiak.freebloks.donate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.databinding.DonateActivityBinding
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.toPixel
import de.saschahlusiak.freebloks.utils.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class DonateActivity : AppCompatActivity() {
    private val binding by viewBinding(DonateActivityBinding::inflate)

    @Inject
    lateinit var analytics: AnalyticsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        analytics.logEvent("donate_show", null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding) {
            donationFreebloksVip.setOnClickListener { onFreebloksVIPClick() }
            donationPaypal.setOnClickListener { onPayPalClick() }

            if (Global.IS_GOOGLE) {
                // Google does not allow to pay with any other payment provider, so we fast track to
                // FreebloksVIP.
                donateThankYou.isVisible = true
                donationsGroup.isVisible = true
                donationPaypal.isVisible = false

                donateButtonGroup.isVisible = false

                next.setOnClickListener { onFreebloksVIPClick() }
                skip.setOnClickListener { onSkipButtonPress() }

            } else {
                next.setOnClickListener { onYesButtonPress() }
                skip.setOnClickListener { onSkipButtonPress() }
            }

            if (savedInstanceState == null) {
                donateIcon.scaleX = 0.0f
                donateIcon.scaleY = 0.0f
                donateIcon.animate()
                    .setStartDelay(200)
                    .setDuration(1100)
                    .setInterpolator(OvershootInterpolator())
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .start()

                donateQuestion.alpha = 0.0f
                donateQuestion.translationY = (-16.0f).toPixel(this@DonateActivity)
                donateQuestion.animate()
                    .setStartDelay(700)
                    .setDuration(800)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .alpha(1.0f)
                    .translationY(0.0f)
                    .start()

                donateButtonGroup.alpha = 0.0f
                donateButtonGroup.translationY = (-16.0f).toPixel(this@DonateActivity)
                donateButtonGroup.animate()
                    .setStartDelay(750)
                    .setDuration(800)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .alpha(1.0f)
                    .translationY(0.0f)
                    .start()

                if (Global.IS_GOOGLE) {
                    donationsGroup.alpha = 0.0f
                    donationsGroup.animate()
                        .setStartDelay(1300)
                        .setDuration(2500)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .alpha(1.0f)
                        .start()

                    donateThankYou.alpha = 0.0f
                    donateThankYou.animate()
                        .setStartDelay(1300)
                        .setDuration(2500)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .alpha(1.0f)
                        .start()
                }
            }
        }
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

    private fun onYesButtonPress() {
        analytics.logEvent("donate_yes", null)

        with(binding) {
            block1.isVisible = false
            donateButtonGroup.isVisible = false
            donationsGroup.isVisible = true
            donateThankYou.isVisible = true
        }
    }

    private fun onSkipButtonPress() {
        analytics.logEvent("donate_skip", null)

        finish()
    }

    private fun onFreebloksVIPClick() {
        analytics.logEvent("donate_freebloksvip", null)
        startActivity(freebloksVipIntent)
    }

    private fun onPayPalClick() {
        analytics.logEvent("donate_paypal", null)
        startActivity(paypalIntent)
    }

    internal val freebloksVipIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_FREEBLOKS_VIP))
    internal val paypalIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PAYPAL))

    companion object {
        // F-Droid does not have a Freebloks VIP, so redirect the user to the Google Play Store.
        private val URL_FREEBLOKS_VIP = if (Global.IS_FDROID)
            "https://play.google.com/store/apps/details?id=de.saschahlusiak.freebloksvip"
        else
            Global.getMarketURLString("de.saschahlusiak.freebloksvip")

        private const val URL_PAYPAL = "https://paypal.me/saschahlusiak/3eur"
    }
}