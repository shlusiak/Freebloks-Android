package de.saschahlusiak.freebloks.game.dialogs

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_2_COLORS_2_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_2_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_4_COLORS_4_PLAYERS
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_DUO
import de.saschahlusiak.freebloks.model.GameMode.GAMEMODE_JUNIOR
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.utils.Dialog

@Composable
private fun SwitchListItem(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(onClick = { onCheckedChange(!checked) })
            .heightIn(min = 52.dp)
            .padding(
                horizontal = dimensionResource(id = R.dimen.dialog_padding),
                vertical = 8.dp
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f)
        )

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ColorListItem(
    color: StoneColor,
    checkable: Boolean,
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
        AnimatedVisibility(visible = checkable) {
            Checkbox(
                checked = checked,
                modifier = Modifier.padding(end = 8.dp),
                onCheckedChange = { onClick(!checked) }
            )
        }

        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Surface(
            color = colorResource(id = color.foregroundColorId),
            shape = CircleShape,
            shadowElevation = 8.dp,
        ) {
            Spacer(modifier = Modifier.size(42.dp))
        }
    }
}

@Composable
private fun DropDown(
    labels: Array<String>,
    selection: Int,
    modifier: Modifier,
    onSelected: (Int) -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
    ) {
        OutlinedButton(
            onClick = { isExpanded = true },
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = labels[selection])
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            labels.forEachIndexed { index, label ->
                DropdownMenuItem(text = { Text(label) }, onClick = {
                    onSelected(index)
                    isExpanded = false
                })
            }
        }
    }
}

@Composable
private fun GameModeDropDown(
    modifier: Modifier,
    gameMode: GameMode,
    onGameMode: (GameMode) -> Unit
) {
    val labels = stringArrayResource(id = R.array.game_modes)
    DropDown(labels = labels, selection = gameMode.ordinal, modifier = modifier) {
        onGameMode(GameMode.from(it))
    }
}

@Composable
private fun SizesDropDown(
    modifier: Modifier,
    size: Int,
    onSize: (Int) -> Unit
) {
    val sizes = GameConfig.FIELD_SIZES
    val labels = stringArrayResource(id = R.array.game_field_sizes)
    DropDown(labels = labels, selection = sizes.indexOf(size), modifier = modifier) {
        onSize(sizes[it])
    }
}

@Composable
fun ColorListContent(
    defaultMode: GameMode = GAMEMODE_4_COLORS_4_PLAYERS,
    defaultSize: Int = GameMode.DEFAULT.defaultBoardSize(),
    onStartGame: (GameMode, Int, BooleanArray?) -> Unit
) {
    val buttonSize = dimensionResource(id = R.dimen.main_menu_button_height)
    val padding = dimensionResource(id = R.dimen.dialog_padding)

    Dialog {
        var multiplePlayers by rememberSaveable { mutableStateOf(false) }
        var gameMode by rememberSaveable { mutableStateOf(defaultMode) }
        var players by rememberSaveable { mutableStateOf(BooleanArray(4) { false }) }
        var size by rememberSaveable { mutableIntStateOf(defaultSize) }

        val colors = remember(gameMode) {
            stoneColors(gameMode)
        }

        val scrollState = rememberScrollState()

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Row(
                modifier = Modifier
                    .padding(top = padding, start = padding, end = padding)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                GameModeDropDown(
                    modifier = Modifier.weight(1f),
                    gameMode = gameMode
                ) {
                    gameMode = it
                    size = gameMode.defaultBoardSize()
                    players = BooleanArray(4) { false }
                }

                SizesDropDown(
                    modifier = Modifier
                        .padding(start = 8.dp),
                    size = size
                ) {
                    size = it
                }
            }

            SwitchListItem(
                text = stringResource(id = R.string.multiple_players),
                checked = multiplePlayers,
            ) { multiplePlayers = it }

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
                        val players = BooleanArray(4)
                        players[playerIndex] = true
                        onStartGame(gameMode, size, players)
                    }
                }
            }

            Button(
                onClick = {
                    if (multiplePlayers) {
                        onStartGame(gameMode, size, players)
                    } else {
                        onStartGame(gameMode, size, null)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding)
                    .padding(top = 4.dp, bottom = padding)
                    .heightIn(min = buttonSize)
            ) {
                Text(stringResource(id = if (multiplePlayers) R.string.start else R.string.random_color))
            }
        }
    }
}

@Stable
private fun stoneColors(gameMode: GameMode) = when (gameMode) {
    GAMEMODE_2_COLORS_2_PLAYERS -> listOf(
        StoneColor.Blue,
        StoneColor.Red
    )

    GAMEMODE_DUO,
    GAMEMODE_JUNIOR -> listOf(
        StoneColor.Orange,
        StoneColor.Purple
    )

    GAMEMODE_4_COLORS_2_PLAYERS,
    GAMEMODE_4_COLORS_4_PLAYERS -> listOf(
        StoneColor.Blue,
        StoneColor.Yellow,
        StoneColor.Red,
        StoneColor.Green
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "DE")
private fun Preview() {
    AppTheme {
        ColorListContent { gameMode, size, stoneColors ->

        }
    }
}