package de.saschahlusiak.freebloks.game.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.StoneColor

@Composable
internal fun ColorListItem(
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
            .heightIn(min = 48.dp)
            .padding(
                horizontal = dimensionResource(id = R.dimen.dialog_padding),
                vertical = 4.dp
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
