package de.saschahlusiak.freebloks.statistics

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.game.newgame.GameModeDropDown
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.utils.Previews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatisticsScreen(
    data: List<RowData>,
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun StatisticsContent(
    modifier: Modifier = Modifier,
    gameMode: GameMode,
    data: List<RowData>,
    onGameMode: (GameMode) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
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

internal val previewData: List<RowData>
    @Composable get() {
        return listOf(
            RowData(R.string.statistics_label_games_played, "123"),
            RowData(R.string.statistics_label_1st, "5.0%"),
            RowData(R.string.statistics_label_2nd, "5.0%"),
            RowData(R.string.statistics_label_3rd, "6.0%"),
            RowData(R.string.statistics_label_points_total, "96"),
        )
    }

@Composable
@Previews
private fun Preview() {
    MaterialTheme {
        StatisticsScreen(
            data = previewData,
            gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
            signedIn = true,
            onBack = { },
            onSignIn = { },
            onLeaderboards = { },
            onAchievements = { },
            setGameMode = {}
        )
    }
}
