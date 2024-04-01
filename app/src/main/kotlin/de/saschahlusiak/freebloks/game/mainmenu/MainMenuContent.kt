package de.saschahlusiak.freebloks.game.mainmenu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun MainMenuContent(
    soundOn: Boolean,
    canResume: Boolean,
    title: String,
    titleOutlined: Boolean,
    onTitleClick: () -> Unit,
    onNewGame: () -> Unit,
    onResumeGame: () -> Unit,
    onMultiplayer: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onToggleSound: () -> Unit
) {
    val buttonSize = dimensionResource(id = R.dimen.main_menu_button_height)
    Dialog {
        Column(
            Modifier.padding(dimensionResource(id = R.dimen.main_menu_padding))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (titleOutlined) {
                    OutlinedButton(
                        onClick = onTitleClick,
                        modifier = Modifier
                    ) { Text(title) }
                } else {
                    TextButton(
                        onClick = onTitleClick,
                        modifier = Modifier
                    ) { Text(title) }
                }

                Spacer(Modifier.weight(1f))

                FilledIconToggleButton(
                    checked = soundOn,
                    onCheckedChange = { onToggleSound() },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(buttonSize)
                ) {
                    Icon(
                        painter = painterResource(id = if (soundOn) R.drawable.ic_volume_up else R.drawable.ic_volume_off),
                        contentDescription = null
                    )
                }

                FilledIconButton(onClick = onHelp, modifier = Modifier.size(buttonSize)) {
                    Icon(painterResource(id = R.drawable.ic_questionmark), contentDescription = null)
                }
            }

            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNewGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = buttonSize),
                    content = { Text(stringResource(id = R.string.new_game)) }
                )

                Button(
                    onClick = onResumeGame,
                    enabled = canResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = buttonSize),
                    content = { Text(stringResource(id = R.string.resume_game)) }
                )

                Button(
                    onClick = onMultiplayer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = buttonSize),
                    content = { Text(stringResource(id = R.string.multiplayer)) }
                )

                Button(
                    onClick = onSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = buttonSize),
                    content = { Text(stringResource(id = R.string.settings)) }
                )
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        MainMenuContent(
            soundOn = true,
            canResume = true,
            title = "Title",
            titleOutlined = true,
            onTitleClick = {},
            onNewGame = {},
            onResumeGame = {},
            onMultiplayer = {},
            onSettings = {},
            onHelp = {},
            onToggleSound = {}
        )
    }
}