package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R


@Composable
internal fun ChatTextField(onChat: (String) -> Unit) {
    var message by rememberSaveable { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            modifier = Modifier.weight(1f),
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
            trailingIcon = {
                FilledIconButton(
                    modifier = Modifier.padding(8.dp),
                    enabled = message.isNotBlank(),
                    onClick = {
                        onChat(message)
                        message = ""
                    }) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "")
                }
            },
            onValueChange = { message = it },
        )
    }
}
