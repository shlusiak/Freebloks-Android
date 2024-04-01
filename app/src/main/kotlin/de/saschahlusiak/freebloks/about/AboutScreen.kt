package de.saschahlusiak.freebloks.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun AboutScreen(
    onLink: (String) -> Unit,
    onDonate: () -> Unit,
    onDismiss: () -> Unit
) {
    val marketUrl = remember { Global.getMarketURLString(BuildConfig.APPLICATION_ID) }
    val githubLink = stringResource(id = R.string.github_link)
    val state = rememberScrollState()

    Dialog {
        Column(
            modifier = Modifier
                .verticalScroll(state)
                .padding(dimensionResource(id = R.dimen.dialog_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.about_freebloks),
                style = MaterialTheme.typography.titleMedium
            )

            Image(
                painter = painterResource(id = R.drawable.appicon_big),
                modifier = Modifier.size(140.dp),
                contentDescription = null
            )

            TextButton(onClick = { onLink(marketUrl) }) {
                Text(
                    marketUrl,
                    textAlign = TextAlign.Center
                )
            }

            TextButton(
                onClick = { onLink(githubLink) },
            ) {
                Text(
                    text = githubLink,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                stringResource(id = R.string.copyright_string),
                style = MaterialTheme.typography.titleSmall
            )

            val email = "apps@saschahlusiak.de"
            TextButton(
                onClick = { onLink("mailto:$email?subject=Freebloks%203D") },
            ) {
                Text(
                    text = email,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                stringResource(id = R.string.special_thanks_to),
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "Alina Bilciurescu\nMartin Hollingsworth\nEgor Ponomarev",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!Global.IS_VIP) {
                    TextButton(onClick = onDonate) {
                        Text(stringResource(id = R.string.prefs_donation))
                    }
                }

                Text(
                    "v" + BuildConfig.VERSION_NAME,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )

                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = android.R.string.ok))
                }

            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        AboutScreen({}, {}, {})
    }
}