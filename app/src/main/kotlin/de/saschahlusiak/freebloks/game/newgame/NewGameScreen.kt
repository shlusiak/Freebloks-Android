package de.saschahlusiak.freebloks.game.newgame

import androidx.compose.foundation.layout.Arrangement.spacedBy
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_2_COLORS_2_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_2_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_4_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_DUO
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_JUNIOR
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.model.defaultStoneSet
import de.saschahlusiak.freebloks.model.stoneColors
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun NewGameScreen(
    defaultMode: GameMode = GAMEMODE_4_COLORS_4_PLAYERS,
    defaultSize: Int = GameMode.DEFAULT.defaultBoardSize(),
    defaultDifficulty: Int = 4,
    onStartGame: (GameConfig) -> Unit
) {
    val padding = MaterialTheme.dimensions.dialogPadding

    var multiplePlayers by rememberSaveable { mutableStateOf(false) }
    var gameMode by rememberSaveable { mutableStateOf(defaultMode) }
    var players by rememberSaveable { mutableStateOf(BooleanArray(4) { false }) }
    var size by rememberSaveable { mutableIntStateOf(defaultSize) }
    var stones by rememberSaveable { mutableStateOf(GameConfig.defaultStonesForMode(defaultMode)) }
    var difficulty by remember { mutableIntStateOf(defaultDifficulty) }

    var showStonesConfig by rememberSaveable { mutableStateOf(false) }

    Dialog {
        val colors = remember(gameMode) { gameMode.stoneColors() }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingSmall)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = padding)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.new_game),
                style = MaterialTheme.typography.titleMedium
            )

            GameTypeRow(
                modifier = Modifier
                    .padding(horizontal = padding)
                    .align(alignment = Alignment.CenterHorizontally),
                gameMode = gameMode,
                size = size,
                onGameMode = {
                    gameMode = it
                    size = gameMode.defaultBoardSize()
                    stones = gameMode.defaultStoneSet()
                    players = BooleanArray(4) { false }
                },
                onSize = { size = it }
            )

            SwitchListItem(
                text = stringResource(id = R.string.multiple_players),
                checked = multiplePlayers,
            ) { multiplePlayers = it }

            Column {
                colors.forEachIndexed { index, stoneColor ->
                    val playerIndex = when (gameMode) {
                        GAMEMODE_2_COLORS_2_PLAYERS,
                        GAMEMODE_DUO,
                        GAMEMODE_JUNIOR -> index * 2

                        else -> index
                    }

                    ColorListItem(
                        stoneColor,
                        checkable = multiplePlayers,
                        players[playerIndex]
                    ) {
                        if (multiplePlayers) {
                            players = players.clone().apply {
                                this[playerIndex] = it

                                if (gameMode == GAMEMODE_4_COLORS_2_PLAYERS) {
                                    this[(playerIndex + 2) % 4] = it
                                }
                            }
                        } else {
                            players = BooleanArray(4)
                            players[playerIndex] = true
                            onStartGame(
                                GameConfig(
                                    isLocal = true,
                                    server = null,
                                    gameMode = gameMode,
                                    showLobby = false,
                                    requestPlayers = players,
                                    difficulty = difficulty,
                                    stones = stones,
                                    fieldSize = size
                                )
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = MaterialTheme.dimensions.innerPaddingSmall)
            )

            DifficultySlider(difficulty) { difficulty = it }

            Spacer(Modifier.padding(bottom = MaterialTheme.dimensions.innerPaddingSmall))

            Row(
                modifier = Modifier
                    .padding(horizontal = padding)
                    .padding(bottom = padding),
                horizontalArrangement = spacedBy(MaterialTheme.dimensions.innerPaddingMedium)
            ) {
                OutlinedIconButton(
                    onClick = { showStonesConfig = true },
                    modifier = Modifier
                        .size(MaterialTheme.dimensions.buttonSize),
                    colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    enabled = gameMode != GAMEMODE_JUNIOR
                ) {
                    Icon(Icons.Filled.Build, null)
                }

                Button(
                    onClick = {
                        onStartGame(
                            GameConfig(
                                isLocal = true,
                                server = null,
                                gameMode = gameMode,
                                showLobby = false,
                                requestPlayers = players.takeIf { multiplePlayers },
                                difficulty = difficulty,
                                stones = stones,
                                fieldSize = size
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = MaterialTheme.dimensions.buttonSize)
                ) {
                    Text(stringResource(id = if (multiplePlayers) R.string.start else R.string.random_color))
                }
            }
        }
    }

    if (showStonesConfig) {
        Dialog(onDismissRequest = { showStonesConfig = false }) {
            AppTheme {
                StonesConfigScreen(
                    stones,
                    onCancel = { showStonesConfig = false },
                    onOk = {
                        stones = it
                        showStonesConfig = false
                    }
                )
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        NewGameScreen { }
    }
}