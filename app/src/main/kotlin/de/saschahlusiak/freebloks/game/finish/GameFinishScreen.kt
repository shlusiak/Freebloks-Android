package de.saschahlusiak.freebloks.game.finish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun GameFinishScreen(
    gameMode: GameMode,
    data: List<PlayerScore>,
    isSignedIn: Boolean,
    onClose: () -> Unit,
    onNewGame: () -> Unit,
    onMainMenu: () -> Unit,
    onAchievements: () -> Unit,
    onLeaderboards: () -> Unit,
    onStatistics: () -> Unit
) {
    Dialog(horizontalPadding = 0.dp) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(id = R.dimen.dialog_padding)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Image(painterResource(id = R.drawable.award_ribbon), null)

                val place = remember(data) {
                    data.firstOrNull { it.isLocal }?.place
                }
                Text(
                    if (place != null)
                        stringArrayResource(id = R.array.places)[place - 1]
                    else
                        stringResource(id = R.string.game_finished),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                }
            }

            data.forEach {
                PlayerRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    gameMode,
                    it
                )
            }

            Row(
                Modifier.padding(top = 12.dp),
                horizontalArrangement = spacedBy(6.dp)
            ) {
                if (isSignedIn) {
                    FilledTonalIconButton(onClick = onAchievements) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play_games_badge_achievements_white),
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(onClick = onLeaderboards) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play_games_badge_leaderboards_white),
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(onClick = onStatistics) {
                        Icon(painter = painterResource(id = R.drawable.ic_preferences_stats), contentDescription = null)
                    }
                } else {
                    TextButton(onClick = onMainMenu) {
                        Text(stringResource(id = R.string.main_menu))
                    }
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(onClick = onNewGame) {
                    Text(stringResource(id = R.string.play_again))
                }
            }
        }
    }
}

private val previewScores = listOf(
    PlayerScore(
        color1 = 0,
        color2 = 2,
        totalPoints = 19,
        stonesLeft = 3,
        turnsLeft = 1,
        bonus = 15,
        isPerfect = true,
        clientName = "Sascha",
        place = 1,
        isLocal = true
    ),
    PlayerScore(
        color1 = 1,
        color2 = 3,
        totalPoints = 14,
        stonesLeft = 4,
        turnsLeft = 2,
        bonus = 0,
        isPerfect = false,
        clientName = null,
        place = 2,
        isLocal = false
    ),
)

@Composable
@Previews
private fun Preview() {
    AppTheme {
        GameFinishScreen(
            gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
            data = previewScores,
            isSignedIn = false,
            onClose = {},
            onNewGame = {},
            onMainMenu = {},
            onAchievements = {},
            onLeaderboards = {},
            onStatistics = {}
        )
    }
}