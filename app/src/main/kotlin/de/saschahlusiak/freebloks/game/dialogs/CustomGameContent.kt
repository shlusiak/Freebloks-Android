package de.saschahlusiak.freebloks.game.dialogs

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.model.stoneColors
import de.saschahlusiak.freebloks.utils.Dialog


@Composable
internal fun ColorGridItem(
    color: StoneColor,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Boolean) -> Unit
) {
    val label = stringResource(id = color.labelResId)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = { onClick(!checked) })
            .heightIn(min = 52.dp)
            .padding(
                horizontal = dimensionResource(id = R.dimen.dialog_padding),
                vertical = 8.dp
            )
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick(!checked) }
        )

        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

    }
}

@Composable
fun CustomGameContent(
    defaultMode: GameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
    defaultSize: Int = GameMode.DEFAULT.defaultBoardSize(),
    onCancel: () -> Unit,
    onStartGame: (GameMode, Int, BooleanArray?) -> Unit
) {
    val buttonSize = dimensionResource(id = R.dimen.main_menu_button_height)
    val padding = dimensionResource(id = R.dimen.dialog_padding)

    Dialog {
        var gameMode by rememberSaveable { mutableStateOf(defaultMode) }
        var players by rememberSaveable {
            mutableStateOf(
                BooleanArray(4) { false }.apply {
                    when (gameMode) {
                        GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> (Math.random() * 2.0).toInt() * 2
                        GameMode.GAMEMODE_DUO,
                        GameMode.GAMEMODE_JUNIOR -> (Math.random() * 2.0).toInt() * 2

                        GameMode.GAMEMODE_4_COLORS_2_PLAYERS -> (Math.random() * 2.0).toInt()
                        GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> (Math.random() * 4.0).toInt()
                    }.also {
                        this[it] = true

                        if (gameMode === GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
                            this[2] = this[0]
                            this[3] = this[1]
                        }
                    }
                }
            )
        }
        var size by rememberSaveable { mutableIntStateOf(defaultSize) }

        val colors = remember(gameMode) {
            gameMode.stoneColors()
        }

        val scrollState = rememberScrollState()

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text(
                modifier = Modifier
                    .padding(all = padding)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.custom_game),
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
                    players = BooleanArray(4) { false }
                },
                onSize = { size = it }
            )

            colors.chunked(2).forEachIndexed { row, colors ->
                Row {
                    colors.forEachIndexed { column, stoneColor ->
                        val index = row * 2 + column
                        val playerIndex = when (gameMode) {
                            GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                            GameMode.GAMEMODE_DUO,
                            GameMode.GAMEMODE_JUNIOR -> index * 2

                            else -> index
                        }

                        ColorGridItem(
                            color = stoneColor,
                            checked = players[playerIndex],
                            modifier = Modifier.weight(1f)
                        ) {
                            players = players.clone().apply {
                                this[playerIndex] = it

                                if (gameMode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
                                    this[(playerIndex + 2) % 4] = it
                                }
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding)
                    .padding(top = 4.dp, bottom = padding)

            ) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(id = android.R.string.cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = { onStartGame(gameMode, size, players) }) {
                    Text(stringResource(id = R.string.start))
                }
            }

        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "DE")
private fun Preview() {
    AppTheme {
        CustomGameContent(
            onStartGame = { gameMode, size, stoneColors -> },
            onCancel = {}
        )
    }
}