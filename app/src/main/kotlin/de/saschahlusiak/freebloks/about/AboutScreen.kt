package de.saschahlusiak.freebloks.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.utils.Dialog

@Composable
fun AboutScreen(
    onLink: (String) -> Unit,
    onSupport: () -> Unit,
    onDismiss: () -> Unit
) {
    val marketUrl = remember { Global.getMarketURLString(BuildConfig.APPLICATION_ID) }
    val githubLink = stringResource(id = R.string.github_link)
    val state = rememberScrollState()

    Dialog {
        Column(
            modifier = Modifier
                .verticalScroll(state)
                .padding(MaterialTheme.dimensions.dialogPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(id = R.string.about_freebloks),
                style = MaterialTheme.typography.titleMedium
            )

            Image(
                painter = painterResource(id = R.drawable.appicon_big),
                modifier = Modifier.size(100.dp),
                contentDescription = null
            )

            Text(
                "v" + BuildConfig.VERSION_NAME,
                textAlign = TextAlign.Left,
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
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
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
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
                "Alina Bilciurescu\nEgor Ponomarev\nEnzo (zh-tw)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End)
                    .width(intrinsicSize = IntrinsicSize.Max),
                horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
            ) {
                if (!Global.IS_VIP) {
                    OutlinedButton(
                        onClick = onSupport,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = MaterialTheme.dimensions.buttonSize),
                    ) {
                        Text(stringResource(id = R.string.prefs_support))
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = MaterialTheme.dimensions.buttonSize),
                ) {
                    Text(stringResource(id = android.R.string.ok))
                }

            }
        }
    }
}

@Composable
@Preview
fun Preview() {
    AppTheme {
        AboutScreen({}, {}, {})
    }
}