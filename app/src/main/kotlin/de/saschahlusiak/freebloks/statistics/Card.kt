package de.saschahlusiak.freebloks.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.utils.Previews

@Composable
internal fun RowScope.Card(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    percent: String? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(color)
) {
    Card(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.dimensions.innerPaddingMedium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    label,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )

                Text(
                    value,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )

                if (percent != null) {
                    Text(
                        percent,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Row {
            Card(
                label = "Games total",
                value = "17",
                modifier = Modifier,
                percent = "99%"
            )
        }
    }
}