package de.saschahlusiak.freebloks.game.newgame

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
private fun SizeSelector(
    size: Int,
    count: Int,
    onUpdateCount: (Int) -> Unit
) {
    val labelRes = when (size) {
        1 -> R.string.monomino
        2 -> R.string.domino
        3 -> R.string.tromino
        4 -> R.string.tetromino
        5 -> R.string.pentomino
        else -> throw IllegalArgumentException("Invalid size $size")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            size.toString(),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            stringResource(id = labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )

        FilledTonalIconButton(
            onClick = { onUpdateCount(count - 1) },
            enabled = count > 0
        ) { Text("-") }

        Text(
            "$count",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        FilledTonalIconButton(
            onClick = { onUpdateCount(count + 1) },
            enabled = count < 5
        ) { Text("+") }
    }
}

@Composable
fun StonesConfigScreen(
    stones: IntArray,
    onCancel: () -> Unit,
    onOk: (IntArray) -> Unit
) {
    var selections by rememberSaveable(stones) {
        val s = IntArray(Shape.SIZE_MAX)
        stones.forEachIndexed { index, count ->
            s[Shape.get(index).size - 1] = count
        }
        mutableStateOf(s)
    }

    Dialog {
        Column(modifier = Modifier.padding(dimensionResource(id = R.dimen.dialog_padding))) {

            Text(
                stringResource(id = R.string.quantities),
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleMedium
            )

            selections.forEachIndexed { index, count ->
                SizeSelector(size = index + 1, count = count) {
                    selections = selections.copyOf().apply {
                        this[index] = it
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(id = android.R.string.cancel))
                }

                Spacer(Modifier.padding(4.dp))

                OutlinedButton(onClick = {
                    onOk(
                        IntArray(Shape.COUNT) {
                            selections[Shape.get(it).size - 1]
                        }
                    )
                }) {
                    Text(stringResource(id = android.R.string.ok))
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        StonesConfigScreen(GameConfig.DEFAULT_STONE_SET, {}, {})
    }
}