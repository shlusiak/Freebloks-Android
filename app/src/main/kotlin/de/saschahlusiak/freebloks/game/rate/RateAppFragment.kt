package de.saschahlusiak.freebloks.game.rate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.donate.DonateFragment
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews
import de.saschahlusiak.freebloks.utils.prefs
import javax.inject.Inject

@AndroidEntryPoint
class RateAppFragment : DialogFragment() {

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    @Inject
    lateinit var analytics: AnalyticsProvider

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)

        view as ComposeView
        view.setContent { Content() }

        analytics.logEvent("rate_show", null)
    }

    @Composable
    @Previews
    private fun Content() {
        AppTheme {
            Dialog {
                Column(
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.dialog_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(id = R.string.rate_freebloks_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Image(
                        painter = painterResource(id = R.drawable.appicon_big), contentDescription = null,
                        Modifier.size(100.dp)
                    )

                    Text(
                        stringResource(id = R.string.rate_freebloks_text),
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        for (i in 1..5) {
                            Image(painter = painterResource(id = R.drawable.ic_star), contentDescription = null)
                        }
                    }

                    ButtonRow()

                    Spacer(Modifier.padding(8.dp))

                    Text(
                        stringResource(id = R.string.donation_text_line1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )

                    if (!Global.IS_VIP) {
                        HorizontalDivider(
                            Modifier.padding(top = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                analytics.logEvent("rate_donate_click", null)

                                DonateFragment().show(parentFragmentManager, null)
                            },
                        ) {
                            Text(stringResource(id = R.string.rate_freebloks_donate_link))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ButtonRow() {
        Row {
            FilledTonalButton(onClick = {
                analytics.logEvent("rate_no_click", null)
                prefs.edit()
                    .putBoolean("rate_show_again", false)
                    .apply()

                dismiss()
            }, Modifier.weight(1f)) {
                Text(stringResource(id = R.string.rate_never))
            }

            TextButton(onClick = {
                analytics.logEvent("rate_later_click", null)
                dismiss()
            }, Modifier.weight(1f)) {
                Text(stringResource(id = R.string.rate_later))
            }

            FilledTonalButton(onClick = {
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
            }, Modifier.weight(1f)) {
                Text(stringResource(id = R.string.rate_now))
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