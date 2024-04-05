package de.saschahlusiak.freebloks.statistics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.game.newgame.GameModeDropDown
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.utils.Previews

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StatisticsContent(
    modifier: Modifier = Modifier,
    gameMode: GameMode,
    data: List<RowData>,
    onGameMode: (GameMode) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize(),) {
        item(key = "gamemode") {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                GameModeDropDown(
                    modifier = Modifier
                        .align(alignment = Alignment.Center)
                        .fillMaxWidth(0.6f),
                    gameMode = gameMode, onGameMode = onGameMode
                )
            }
        }

        data.forEachIndexed { index, (label, value) ->
            item(key = label) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                        .animateItemPlacement()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (value != null) Color.Unspecified else MaterialTheme.colorScheme.outlineVariant
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = value ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (index != data.lastIndex) {
                item { HorizontalDivider() }
            }
        }

        if (data.isEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Surface {
            val labels = stringArrayResource(id = R.array.statistics_labels)
            StatisticsContent(
                data = listOf(
                    RowData(labels[0], "123"),
                    RowData(labels[1], "5.0%"),
                    RowData(labels[2], "6.0%"),
                    RowData(labels[3], "96"),
                    RowData(labels[4], null)
                ),
                gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                onGameMode = {}
            )
        }
    }
}