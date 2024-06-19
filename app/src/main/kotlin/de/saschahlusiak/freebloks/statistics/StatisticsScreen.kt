package de.saschahlusiak.freebloks.statistics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.game.newgame.GameModeDropDown
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.utils.Previews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatisticsScreen(
    data: StatisticsData?,
    gameMode: GameMode,
    signedIn: Boolean?,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit,
    setGameMode: (GameMode) -> Unit
) {
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.statistics)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        if (signedIn == true) {
                            IconButton(
                                onClick = onLeaderboards,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_leaderboard),
                                    contentDescription = null
                                )
                            }

                            IconButton(
                                onClick = onAchievements,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_achievements),
                                    contentDescription = null,
                                )
                            }
                        }

                        if (signedIn == false) {
                            TextButton(onClick = { onSignIn() }) {
                                Text(text = stringResource(id = R.string.google_play_games_signin))
                            }
                        }
                    }
                )
            }
        ) { padding ->
            StatisticsContent(
                modifier = Modifier.padding(padding),
                gameMode = gameMode,
                data = data,
                onGameMode = setGameMode
            )
        }
    }
}

@Composable
internal fun StatisticsContent(
    modifier: Modifier = Modifier,
    gameMode: GameMode,
    data: StatisticsData?,
    onGameMode: (GameMode) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            GameModeDropDown(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .fillMaxWidth(0.6f),
                gameMode = gameMode,
                onGameMode = onGameMode
            )
        }

        if (data != null) {
            StatisticsTable(data)
        } else {
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

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.StatisticsTable(data: List<RowData>) {
    data.forEachIndexed { index, (label, value) ->
        item(key = label) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .animateItemPlacement()
            ) {
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Unspecified
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (index != data.lastIndex) {
            item { HorizontalDivider() }
        }
    }
}

@Composable
private fun ColumnScope.ColorRow(modifier: Modifier, data: List<Pair<StoneColor, Percent>>) {
    if (data.isEmpty()) return
    val res = LocalContext.current.resources

    Text(
        text = "Wins by colour",
        modifier = Modifier.align(Alignment.CenterHorizontally),
        style = MaterialTheme.typography.titleMedium
    )

    Row(
        modifier,
        horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        data.forEachIndexed { index, (color, percent) ->
            if (index > 0) {
                Text(
                    ">",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                label = color.getName(res),
                value = "$percent%",
                color = color.backgroundColor,
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun ColumnScope.PlacesRow(modifier: Modifier, totalGames: Int, places: List<Int>) {
    Row(
        modifier,
        horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        places.forEachIndexed { index, times ->
            if (index > 0) {
                Text(
                    ">",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val percent = ((100 * times) / totalGames.coerceAtLeast(1))

            val label = when (index) {
                0 -> R.string.statistics_label_1st
                1 -> R.string.statistics_label_2nd
                2 -> R.string.statistics_label_3rd
                else -> R.string.statistics_label_4th
            }

            Card(
                label = stringResource(id = label),
                value = times.toString(),
                percent = "$percent%",
                color = if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
            )
        }
    }
}

@Composable
private fun StatisticsTable(data: StatisticsData) {
    val margin = MaterialTheme.dimensions.innerPaddingMedium
    Column(
        modifier = Modifier
            .padding(MaterialTheme.dimensions.dialogPadding)
            .fillMaxWidth(),
        verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = spacedBy(margin)
        ) {
            Card(
                label = stringResource(id = R.string.statistics_label_games_played),
                value = data.totalGames.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.secondary
            )

            Card(
                label = stringResource(id = R.string.statistics_label_points_total),
                value = "+${data.totalPoints}",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        PlacesRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            totalGames = data.totalGames,
            places = data.places
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = spacedBy(margin)
        ) {
            Card(
                label = stringResource(id = R.string.statistics_label_perfect_games),
                value = data.perfectGames.toString(),
                percent = "${data.perfectGamesPercent}%",
                color = MaterialTheme.colorScheme.secondary
            )

            Card(
                label = stringResource(id = R.string.statistics_label_good_games),
                value = data.goodGames.toString(),
                percent = "${data.goodGamesPercent}%",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        ColorRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            data.winsByColor
        )
    }
}

@Composable
@Previews
private fun Preview1() {
    AppTheme {
        val data = StatisticsData(
            gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
            totalGames = 140,
            totalPoints = 356,
            places = listOf(2, 51, 63, 0),
            perfectGames = 17,
            goodGames = 5,
            winsByColor = listOf(
                StoneColor.Blue to 40,
                StoneColor.Red to 30,
                StoneColor.Green to 20,
                StoneColor.Yellow to 10,
            )
        )

        Surface {
            StatisticsContent(
                modifier = Modifier,
                data = data,
                gameMode = data.gameMode,
                onGameMode = {}
            )
        }
    }
}
