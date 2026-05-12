package de.saschahlusiak.freebloks.game.rate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.Preferences
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.support.SupportFragment
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews
import javax.inject.Inject

@AndroidEntryPoint
class RateAppFragment : DialogFragment() {

    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    @Inject
    lateinit var prefs: Preferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)

        view as ComposeView
        view.setContent { Content() }
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
                        repeat(5) {
                            Image(painter = painterResource(id = R.drawable.ic_star), contentDescription = null)
                        }
                    }

                    ButtonRow()

                    Spacer(Modifier.height(MaterialTheme.dimensions.innerPaddingMedium))

                    Text(
                        stringResource(id = R.string.support_text_line1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                    )

                    if (!Global.IS_VIP) {
                        HorizontalDivider(
                            Modifier.padding(
                                top = MaterialTheme.dimensions.innerPaddingMedium,
                                bottom = MaterialTheme.dimensions.innerPaddingMedium
                            )
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ElevatedButton(
                                onClick = {
                                    SupportFragment().show(parentFragmentManager, null)
                                },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text(stringResource(id = R.string.rate_freebloks_support_link))
                            }

                            Icon(
                                imageVector = Icons.Outlined.Coffee,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ButtonRow() {
        Row(horizontalArrangement = spacedBy(6.dp)) {
            Button(
                onClick = {
                    prefs.rateShowAgain = false

                    dismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_never))
            }

            Button(
                onClick = ::dismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_later))
            }

            Button(
                onClick = {
                    val intent = Intent(
                        "android.intent.action.VIEW",
                        Global.getMarketURLString(BuildConfig.APPLICATION_ID).toUri()
                    )
                    prefs.rateShowAgain = false

                    dismiss()
                    startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(MaterialTheme.dimensions.buttonSize)
            ) {
                Text(stringResource(id = R.string.rate_now))
            }
        }
    }
}