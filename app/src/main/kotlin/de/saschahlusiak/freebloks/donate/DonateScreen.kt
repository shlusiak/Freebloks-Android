package de.saschahlusiak.freebloks.donate

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun DonateScreen(
    showPaypal: Boolean,
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
            Text(
                text = stringResource(id = R.string.donation_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

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

            Text(
                stringResource(id = R.string.donation_text_short),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = spacedBy(16.dp)
            ) {
                ImageButton(image = R.drawable.logo_freebloks_vip, onClick = onFreebloksVIP)

                if (showPaypal) {
                    ImageButton(image = R.drawable.logo_paypal, onClick = onPaypal)
                }
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
    image: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 2.dp,
        modifier = modifier,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(56.dp)
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