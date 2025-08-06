package de.saschahlusiak.freebloks.statistics

import android.graphics.drawable.Drawable
import android.graphics.drawable.shapes.Shape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.utils.LeaderboardEntry
import de.saschahlusiak.freebloks.utils.Previews


@Composable
internal fun LeaderboardCard(
    modifier: Modifier,
    entry: LeaderboardEntry
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isLocal)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier.padding(MaterialTheme.dimensions.innerPaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
        ) {
            var drawable by remember { mutableStateOf<Drawable?>(null) }
            LaunchedEffect(key1 = entry.iconUri) {
                drawable = entry.fetchImage(entry.iconUri)
            }

            AnimatedContent(targetState = drawable != null,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) }
            ) {
                if (it) {
                    AsyncImage(
                        model = drawable,
                        contentDescription = "",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    ) {}
                }
            }


            Column {
                Text("#${entry.rank}", style = MaterialTheme.typography.labelMedium)
                Text(entry.name, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                pluralStringResource(id = R.plurals.number_of_points, entry.points, entry.points),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
@Previews
private fun Previews() {
    val data = listOf(
        LeaderboardEntry(4, null, "Name 1", 123, false, { null }),
        LeaderboardEntry(5, null, "Name 2", 100, true, { null }),
        LeaderboardEntry(6, null, "Name 3", 96, false, { null }),
    )
    AppTheme {
        Column(
            modifier = Modifier.padding(MaterialTheme.dimensions.dialogPadding),
            verticalArrangement = spacedBy(4.dp)
        ) {
            data.forEach {
                LeaderboardCard(modifier = Modifier.fillMaxWidth(), entry = it)
            }
        }
    }
}