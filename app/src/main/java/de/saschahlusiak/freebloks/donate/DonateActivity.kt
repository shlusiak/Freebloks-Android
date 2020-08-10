package de.saschahlusiak.freebloks.donate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.analytics
import de.saschahlusiak.freebloks.utils.toPixel
import kotlinx.android.synthetic.main.donate_activity.*

class DonateActivity : AppCompatActivity(R.layout.donate_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.logEvent("show_donate", null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        next.setOnClickListener { onNextButtonPress() }
        skip.setOnClickListener { finish() }

        donationFreebloksVip.setOnClickListener { onFreebloksVIPClick() }
        donationPaypal.setOnClickListener { onPayPalClick() }
        donationBitcoin.setOnClickListener { onBitcoinClick() }

        if (savedInstanceState == null) {
            donate_icon.scaleX = 0.0f
            donate_icon.scaleY = 0.0f
            donate_icon.animate()
                .setStartDelay(200)
                .setDuration(1800)
                .setInterpolator(OvershootInterpolator())
                .scaleX(1.0f)
                .scaleY(1.0f)
                .start()

            donate_question.alpha = 0.0f
            donate_question.translationY = (-16.0f).toPixel(this)
            donate_question.animate()
                .setStartDelay(800)
                .setDuration(1200)
                .setInterpolator(FastOutSlowInInterpolator())
                .alpha(1.0f)
                .translationY(0.0f)
                .start()

            donateButtonGroup.alpha = 0.0f
            donateButtonGroup.translationY = (-16.0f).toPixel(this)
            donateButtonGroup.animate()
                .setStartDelay(1000)
                .setDuration(1500)
                .setInterpolator(FastOutSlowInInterpolator())
                .alpha(1.0f)
                .translationY(0.0f)
                .start()
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

    private fun onNextButtonPress() {
        block1.isVisible = false
        donateButtonGroup.isVisible = false
        donationsGroup.isVisible = true
        donateThankYou.isVisible = true
    }

    private fun onFreebloksVIPClick() {
        analytics.logEvent("donate_freebloksvip", null)
        startActivity(freebloksVipIntent)
    }

    private fun onBitcoinClick() {
        analytics.logEvent("donate_bitcoin", null)
        try {
            startActivity(bitcoinIntent)
        } catch (e: Exception) {
            startActivity(bitcoinFallbackIntent)
        }
    }

    private fun onPayPalClick() {
        analytics.logEvent("donate_paypal", null)
        startActivity(paypalIntent)
    }

    private val bitcoinIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_BITCOIN))
    private val bitcoinFallbackIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_BITCOIN_EXPLORER))
    private val freebloksVipIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_FREEBLOKS_VIP))
    private val paypalIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PAYPAL))

    companion object {
        // F-Droid does not have a Freebloks VIP, so redirect the user to the Google Play Store.
        private val URL_FREEBLOKS_VIP = if (Global.IS_FDROID)
            "https://play.google.com/store/apps/details?id=de.saschahlusiak.freebloksvip"
        else
            Global.getMarketURLString("de.saschahlusiak.freebloksvip")

        private const val URL_PAYPAL = "https://paypal.me/saschahlusiak/3eur"
        private const val BITCOIN_WALLET_ADDRESS = "bc1qdgm2zvlc6qzqh8qs44wv8l622tfrhvkjqn0fkl"
        private const val URL_BITCOIN = "bitcoin:$BITCOIN_WALLET_ADDRESS?amount=0.0002"
        private const val URL_BITCOIN_EXPLORER = "https://www.blockchain.com/btc/address/$BITCOIN_WALLET_ADDRESS"
    }
}