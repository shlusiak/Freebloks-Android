package de.saschahlusiak.freebloks.game

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun StatusRow() {
    Row(modifier = Modifier.fillMaxWidth()) {

        Text(text = "Current player")
        
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        StatusRow()
    }
}