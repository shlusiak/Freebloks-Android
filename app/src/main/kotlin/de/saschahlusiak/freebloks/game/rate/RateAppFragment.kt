package de.saschahlusiak.freebloks.game.rate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.donate.DonateFragment
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews
import javax.inject.Inject

@AndroidEntryPoint
class RateAppFragment : DialogFragment() {

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    @Inject
    lateinit var prefs: Preferences

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
                    modifier = Modifier.padding(MaterialTheme.dimensions.dialogPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(id = R.string.rate_freebloks_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Image(
                        painter = painterResource(id = R.drawable.appicon_big), contentDescription = null,
                        Modifier.size(120.dp)
                    )

                    Text(
                        stringResource(id = R.string.rate_freebloks_text),
                        modifier = Modifier.padding(top = MaterialTheme.dimensions.innerPaddingMedium),
                        textAlign = TextAlign.Center
                    )

                    Row(modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingMedium)) {
                        for (i in 1..5) {
                            Image(painter = painterResource(id = R.drawable.ic_star), contentDescription = null)
                        }
                    }

                    ButtonRow()

                    Spacer(Modifier.height(MaterialTheme.dimensions.innerPaddingMedium))

                    Text(
                        stringResource(id = R.string.donation_text_line1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )

                    if (!Global.IS_VIP) {
                        HorizontalDivider(
                            Modifier.padding(top = MaterialTheme.dimensions.innerPaddingMedium)
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
            FilledTonalButton(
                onClick = {
                    analytics.logEvent("rate_no_click", null)
                    prefs.rateShowAgain = false

                    dismiss()
                },
                Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_never))
            }

            TextButton(
                onClick = {
                    analytics.logEvent("rate_later_click", null)
                    dismiss()
                },
                Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_later))
            }

            FilledTonalButton(
                onClick = {
                    analytics.logEvent("rate_yes_click", null)
                    val intent = Intent(
                        "android.intent.action.VIEW",
                        Uri.parse(Global.getMarketURLString(BuildConfig.APPLICATION_ID))
                    )
                    prefs.rateShowAgain = false

                    dismiss()
                    startActivity(intent)
                },
                Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_now))
            }
        }
    }
}