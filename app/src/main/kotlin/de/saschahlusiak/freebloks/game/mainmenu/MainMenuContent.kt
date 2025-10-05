package de.saschahlusiak.freebloks.game.mainmenu

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
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
    val buttonSize = MaterialTheme.dimensions.mainMenuButtonHeight
    val innerPadding = MaterialTheme.dimensions.mainMenuPadding
    Dialog(horizontalPadding = 24.dp) {
        Column(
            Modifier.padding(innerPadding),
            verticalArrangement = spacedBy(MaterialTheme.dimensions.mainMenuButtonMargin)
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
                        .padding(end = MaterialTheme.dimensions.mainMenuButtonMargin)
                        .size(buttonSize)
                ) {
                    Icon(
                        if (soundOn) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = if (soundOn) LocalContentColor.current else LocalContentColor.current.copy(alpha = 0.75f)
                    )
                }

                FilledIconButton(
                    onClick = onHelp,
                    modifier = Modifier
                        .size(buttonSize)
                ) {
                    Icon(Icons.Rounded.QuestionMark, contentDescription = null)
                }
            }

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