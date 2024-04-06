package de.saschahlusiak.freebloks.donate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.toPixel
import javax.inject.Inject

@AndroidEntryPoint
class DonateFragment : DialogFragment() {
    @Inject
    lateinit var analytics: AnalyticsProvider

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics.logEvent("donate_show", null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view as ComposeView
        dialog?.window?.setBackgroundDrawable(null)
        view.setContent {
            AppTheme {
                DonateScreen(
                    showPaypal = true, // !Global.IS_GOOGLE,
                    onDismiss = ::onSkipButtonPress,
                    onFreebloksVIP = ::onFreebloksVIPClick,
                    onPaypal = ::onPayPalClick,
                    onLink = ::onLink
                )
            }
        }
    }

    private fun onLink(link: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
        )
    }

    private fun onSkipButtonPress() {
        analytics.logEvent("donate_skip", null)

        dismiss()
    }

    private fun onFreebloksVIPClick() {
        analytics.logEvent("donate_freebloksvip", null)
        startActivity(freebloksVipIntent)
    }

    private fun onPayPalClick() {
        analytics.logEvent("donate_paypal", null)
        startActivity(paypalIntent)
    }

    private val freebloksVipIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_FREEBLOKS_VIP))
    private val paypalIntent get() = Intent(Intent.ACTION_VIEW, Uri.parse(URL_PAYPAL))

    companion object {
        // F-Droid does not have a Freebloks VIP, so redirect the user to the Google Play Store.
        private val URL_FREEBLOKS_VIP = if (Global.IS_FDROID)
            "https://play.google.com/store/apps/details?id=de.saschahlusiak.freebloksvip"
        else
            Global.getMarketURLString("de.saschahlusiak.freebloksvip")

        private const val URL_PAYPAL = "https://paypal.me/saschahlusiak/3eur"
    }
}