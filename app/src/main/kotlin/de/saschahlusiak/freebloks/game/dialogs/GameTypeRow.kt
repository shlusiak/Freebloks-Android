package de.saschahlusiak.freebloks.game.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode

@Composable
fun GameTypeRow(
    modifier: Modifier,
    gameMode: GameMode,
    size: Int,
    onGameMode: (GameMode) -> Unit,
    onSize: (Int) -> Unit
) {
    Row(modifier) {
        GameModeDropDown(
            modifier = Modifier.weight(1f),
            gameMode = gameMode
        ) {
            onGameMode(it)
        }

        SizesDropDown(
            modifier = Modifier
                .padding(start = 8.dp),
            size = size
        ) { onSize(it) }
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
