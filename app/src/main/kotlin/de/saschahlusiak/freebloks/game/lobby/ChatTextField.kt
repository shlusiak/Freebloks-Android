package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.utils.Previews

@Composable
internal fun ChatTextField(modifier: Modifier = Modifier, onChat: (String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }

    OutlinedTextField(
        value = message,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.lobby_message_hint)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions {
            if (message.isNotBlank()) {
                onChat(message)
                message = ""
            }
        },
        shape = CircleShape,
        trailingIcon = {
            FilledIconButton(
                modifier = Modifier.padding(8.dp),
                enabled = message.isNotBlank(),
                onClick = {
                    onChat(message)
                    message = ""
                }) {
                Icon(Icons.Filled.Send, contentDescription = "")
            }
        },
        onValueChange = { message = it },
    )
}

@Previews
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            ChatTextField {

            }
        }
    }
}