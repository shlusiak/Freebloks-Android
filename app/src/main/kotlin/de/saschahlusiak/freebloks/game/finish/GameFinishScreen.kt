package de.saschahlusiak.freebloks.game.finish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
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
                .padding(MaterialTheme.dimensions.dialogPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.dimensions.innerPaddingMedium)
            ) {
                Image(painterResource(id = R.drawable.image_ribbon), null)

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

            data.forEach { player ->
                PlayerRow(
                    modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingSmall),
                    gameMode = gameMode,
                    score = player
                )
            }

            HorizontalDivider(
                Modifier.padding(top = MaterialTheme.dimensions.innerPaddingLarge)
            )

            Row(
                Modifier.padding(top = MaterialTheme.dimensions.innerPaddingLarge),
                horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
            ) {
                FilledTonalIconButton(
                    onClick = onStatistics,
                    modifier = Modifier.size(MaterialTheme.dimensions.buttonSize)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_chart), contentDescription = null)
                }

                if (isSignedIn) {
                    FilledTonalIconButton(
                        onClick = onLeaderboards,
                        modifier = Modifier.size(MaterialTheme.dimensions.buttonSize)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_leaderboard),
                            contentDescription = null
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onAchievements,
                        modifier = Modifier.size(MaterialTheme.dimensions.buttonSize)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_achievements),
                            contentDescription = null
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                OutlinedButton(
                    onClick = onNewGame,
                    modifier = Modifier
                        .heightIn(min = MaterialTheme.dimensions.buttonSize)
                ) {
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
        stonesLeft = 0,
        turnsLeft = 1,
        bonus = 20,
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
    PlayerScore(
        color1 = 3,
        totalPoints = 20,
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
            isSignedIn = true,
            onClose = {},
            onNewGame = {},
            onMainMenu = {},
            onAchievements = {},
            onLeaderboards = {},
            onStatistics = {}
        )
    }
}