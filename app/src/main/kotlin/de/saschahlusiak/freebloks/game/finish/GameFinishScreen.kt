package de.saschahlusiak.freebloks.game.finish

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun GameFinishScreen() {
    Dialog {
        Column(Modifier.padding(dimensionResource(id = R.dimen.dialog_padding))) {

            Text(
                stringResource(id = R.string.game_finished),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        GameFinishScreen(

        )
    }
}