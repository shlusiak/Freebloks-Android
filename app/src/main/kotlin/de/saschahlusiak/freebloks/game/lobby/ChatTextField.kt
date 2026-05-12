package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
internal fun ChatTextField(
    message: MutableState<String>,
    inlineSend: Boolean,
    modifier: Modifier = Modifier,
    onChat: (String) -> Unit
) {
    OutlinedTextField(
        value = message.value,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.lobby_message_hint)) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send
        ),
        singleLine = true,
        keyboardActions = KeyboardActions {
            if (message.value.isNotBlank()) {
                onChat(message.value)
                message.value = ""
            }
        },
        shape = CircleShape,
        trailingIcon = {
            if (inlineSend) {
                FilledIconButton(
                    modifier = Modifier.padding(end = 6.dp),
                    enabled = message.value.isNotBlank(),
                    onClick = {
                        onChat(message.value)
                        message.value = ""
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "")
                }
            }
        },
        onValueChange = { message.value  = it },
    )
}

@Previews
@Composable
private fun Preview() {
    AppTheme {
        Surface {
            Box {
                val message = rememberSaveable { mutableStateOf("") }

                ChatTextField(message, true) { _ -> }
            }
        }
    }
}