package de.saschahlusiak.freebloks.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme

@Composable
fun Dialog(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(min = 300.dp)
            .wrapContentSize(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.dialog_corner_radius)),
        content = content
    )
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Dialog {
            Text(
                "Dialog",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}
