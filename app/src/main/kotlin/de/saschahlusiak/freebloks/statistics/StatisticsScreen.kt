package de.saschahlusiak.freebloks.statistics

import android.net.Uri
import android.widget.Space
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.game.newgame.GameModeDropDown
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.utils.LeaderboardEntry
import de.saschahlusiak.freebloks.utils.Previews

@Composable
internal fun StatisticsContent(
    modifier: Modifier = Modifier,
    gameMode: GameMode,
    data: StatisticsData?,
    gamesData: GooglePlayGamesData?,
    onGameMode: (GameMode) -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit
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
            StatisticsTable(data, gamesData, onSignIn, onSignOut, onLeaderboards, onAchievements)
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

@Composable
private fun ColumnScope.PlacesRow(
    modifier: Modifier,
    totalGames: Int,
    places: List<Int>,
    firstColor: Color,
    firstContentColor: Color = MaterialTheme.colorScheme.contentColorFor(firstColor)
) {
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
                color = if (index == 0) firstColor else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (index == 0) firstContentColor else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun ColumnScope.ColorsRow(
    modifier: Modifier,
    placesByColor: List<Pair<StoneColor, List<Int>>>
) {
    Row(
        modifier,
        horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        placesByColor.forEachIndexed { index, (stoneColor, places) ->
            if (index > 0) {
                Text(
                    ">",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val won = (places.firstOrNull() ?: 0)
            val total = places.sum()
            val percent = ((100 * won) / total.coerceAtLeast(1))

            val color = stoneColor.foregroundColor.copy(alpha = 0.4f)
                .compositeOver(MaterialTheme.colorScheme.inverseSurface)
            val contentColor = MaterialTheme.colorScheme.inverseOnSurface

            Card(
                label = stringResource(id = stoneColor.labelResId),
                value = "$percent%",
                percent = pluralStringResource(id = R.plurals.games, count = total, total),
                color = color,
                contentColor = contentColor
            )
        }
    }
}

@Composable
private fun StatisticsTable(
    data: StatisticsData,
    gamesData: GooglePlayGamesData?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit
) {
    val margin = MaterialTheme.dimensions.innerPaddingMedium
    Column(
        modifier = Modifier
            .padding(MaterialTheme.dimensions.dialogPadding)
            .fillMaxWidth(),
        verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium),
        horizontalAlignment = Alignment.Start
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
            places = data.places,
            firstColor = MaterialTheme.colorScheme.primary
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

        if (data.placesByColor.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingMedium))

            ColorsRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                data.placesByColor
            )
        }

        if (gamesData == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = MaterialTheme.dimensions.innerPaddingLarge)
            )
        }

        AnimatedVisibility(visible = gamesData != null && gamesData.isAvailable, enter = fadeIn()) {
            gamesData ?: return@AnimatedVisibility
            Column(
                verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium),
            ) {
                GooglePlayData(gamesData, onSignIn, onSignOut, onLeaderboards, onAchievements)
            }
        }
    }
}

@Composable
private fun ColumnScope.GooglePlayData(
    data: GooglePlayGamesData,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onLeaderboards: () -> Unit,
    onAchievements: () -> Unit
) {
    HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingMedium))

    if (!data.isSignedIn) {
        TextButton(onClick = { onSignIn() }, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(id = R.string.google_play_games_signin))
        }
        return
    }

    data.leaderboardData.forEach {
        LeaderboardCard(
            modifier = Modifier
                .fillMaxWidth(),
            entry = it
        )
    }

    Row(
        modifier = Modifier.padding(top = MaterialTheme.dimensions.innerPaddingMedium),
        horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
    ) {
        OutlinedButton(
            onClick = onLeaderboards,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(id = R.string.google_play_games_leaderboard))
        }
        OutlinedButton(
            onClick = onAchievements,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = stringResource(id = R.string.google_play_games_achievements))
        }
    }

    TextButton(onClick = { onSignOut() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(text = stringResource(id = R.string.google_play_games_signout))
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        val data = StatisticsData(
            gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
            totalGames = 140,
            totalPoints = 356,
            places = listOf(2, 51, 63, 0),
            perfectGames = 17,
            goodGames = 5,
            placesByColor = listOf(
                StoneColor.Blue to listOf(40, 0, 0, 0),
                StoneColor.Red to listOf(30, 5, 5, 3),
                StoneColor.Green to listOf(20, 7),
                StoneColor.Yellow to listOf(1, 2, 3, 4),
            )
        )

        val gamesData = GooglePlayGamesData(
            isAvailable = true,
            isSignedIn = true,
            leaderboardData = listOf(
                LeaderboardEntry(4, null, "Name 1", 123, false, { null }),
                LeaderboardEntry(5, null, "Name 2", 100, true, { null }),
                LeaderboardEntry(6, null, "Name 3", 96, false, { null }),
            )
        )

        Surface {
            StatisticsContent(
                modifier = Modifier,
                data = data,
                gamesData = gamesData,
                gameMode = data.gameMode,
                onGameMode = {},
                onSignIn = {},
                onSignOut = {},
                onLeaderboards = {},
                onAchievements = {}
            )
        }
    }
}
