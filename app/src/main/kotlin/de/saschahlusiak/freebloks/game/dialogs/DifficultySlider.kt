package de.saschahlusiak.freebloks.game.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import kotlin.math.roundToInt

// the values of the difficulty slider for each index
internal val difficultyValues = intArrayOf(
    200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
)

@Composable
fun DifficultySlider(difficulty: Int, onDifficultyChange: (Int) -> Unit) {
    val index = remember(difficulty) {
        difficultyValues.indexOfFirst { it == difficulty }.takeIf { it >= 0 } ?: difficultyValues.indexOf(10)
    }
    val value = difficultyValues[index]
    val labels = stringArrayResource(id = R.array.difficulties)
    var text = 0
    if (value >= 5) text = 1
    if (value >= 40) text = 2
    if (value >= 80) text = 3
    if (value >= 160) text = 4

    Column(
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.dialog_padding))
    ) {
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = stringResource(id = R.string.difficulty),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = labels[text],
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Column {
            Slider(
                value = index.toFloat(),
                onValueChange = { onDifficultyChange(difficultyValues[it.roundToInt()]) },
                steps = difficultyValues.size - 2,
                modifier = Modifier.padding(top = 4.dp),
                valueRange = 0f..difficultyValues.size.toFloat() - 1f
            )
        }
    }
}
