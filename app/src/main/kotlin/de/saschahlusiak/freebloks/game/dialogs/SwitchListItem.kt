package de.saschahlusiak.freebloks.game.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R

@Composable
internal fun SwitchListItem(
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
