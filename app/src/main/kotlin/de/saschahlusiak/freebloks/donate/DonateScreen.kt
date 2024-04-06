package de.saschahlusiak.freebloks.donate

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

private const val githubSponsor = "https://github.com/sponsors/shlusiak"

@Composable
fun DonateScreen(
    showExtra: Boolean, // Show non-google play links
    onDismiss: () -> Unit,
    onLink: (String) -> Unit,
    onFreebloksVIP: () -> Unit,
    onPaypal: () -> Unit
) {
    Dialog {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(4.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dialog_padding))
        ) {
            Box(Modifier.fillMaxWidth()) {

                Text(
                    text = stringResource(id = R.string.donation_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(alignment = Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                }
            }

            Text(
                stringResource(id = R.string.donation_text_line1),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            val githubLink = stringResource(id = R.string.github_link)
            TextButton(onClick = { onLink(githubLink) }) {
                Text(githubLink, textAlign = TextAlign.Center)
            }

            Text(
                stringResource(id = R.string.donation_text_line2),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            val scale by rememberInfiniteTransition().animateFloat(
                initialValue = 0.8f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    tween(1200, easing = FastOutLinearInEasing),
                    RepeatMode.Reverse
                ), label = ""
            )
            Icon(
                painterResource(id = R.drawable.ic_donate), null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.scale(scale)
            )

            Text(
                stringResource(id = R.string.donation_text_short),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = spacedBy(16.dp)
            ) {

                if (showExtra) {
                    ImageButton(image = R.drawable.logo_github_sponsor, onClick = {
                        onLink(githubSponsor)
                    })

                    ImageButton(image = R.drawable.logo_paypal, onClick = onPaypal)
                }

                ImageButton(image = R.drawable.logo_freebloks_vip, onClick = onFreebloksVIP)
            }

            Text(
                stringResource(id = R.string.donate_thank_you),
                style = MaterialTheme.typography.labelMedium
            )

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(alignment = Alignment.End)
            ) {
                Text(stringResource(id = R.string.donation_skip))
            }
        }
    }
}

@Composable
private fun ImageButton(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        contentPadding = PaddingValues(20.dp),
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
        )
    }
}

@Previews
@Composable
private fun Preview() {
    AppTheme {
        DonateScreen(true, {}, {}, {}, {})
    }
}