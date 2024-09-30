package de.saschahlusiak.freebloks.game.multiplayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.dimensions

@Composable
internal fun RadioButtonListItem(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelected: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(
                onClick = { onSelected() },
                interactionSource = interactionSource,
                indication = null
            )
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected,
            interactionSource = interactionSource
        )

        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
    }
}
