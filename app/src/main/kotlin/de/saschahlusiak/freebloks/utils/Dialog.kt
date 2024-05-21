package de.saschahlusiak.freebloks.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .wrapContentSize(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(MaterialTheme.dimensions.dialogCornerRadius),
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
