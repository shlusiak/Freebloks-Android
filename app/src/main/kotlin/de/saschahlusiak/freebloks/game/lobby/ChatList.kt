package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun ChatList(
    history: List<ChatItem>,
    mode: GameMode,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()

    LaunchedEffect(key1 = history.size) {
        if (history.isNotEmpty()) {
            state.animateScrollToItem(history.lastIndex)
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceDim,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(4.dp)
    ) {
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(8.dp)
        ) {
            history.forEachIndexed { index, item ->
                when (item) {
                    is ChatItem.Generic -> item(key = index, contentType = 1) {
                        GenericMessage(text = item.text)
                    }

                    is ChatItem.Server -> item(key = index, contentType = 1) {
                        GenericMessage(text = item.text)
                    }

                    is ChatItem.Message -> item(key = index, contentType = 2) {
                        Message(
                            message = item,
                            mode = mode
                        )
                    }
                }
            }
        }
    }
}

private val localShape = RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
private val remoteShape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

@Composable
private fun Message(
    message: ChatItem.Message,
    mode: GameMode,
    modifier: Modifier = Modifier
) {
    val isLocal = message.isLocal
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isLocal) Arrangement.End else Arrangement.Start
    ) {
        val color = if (message.player == null)
            StoneColor.White.backgroundColor
        else
            mode.colorOf(message.player).backgroundColor

        Surface(
            color = color,
            shadowElevation = 2.dp,
            shape = if (isLocal) localShape else remoteShape
        ) {
            Column(
                Modifier.padding(
                    horizontal = MaterialTheme.dimensions.innerPaddingLarge,
                    vertical = MaterialTheme.dimensions.innerPaddingSmall
                )
            ) {
                if (!isLocal && message.name.isNotBlank()) {
                    Text(
                        message.name,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Text(
                    text = message.text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = if (message.isLocal) TextAlign.End else TextAlign.Start,
                )
            }
        }
    }
}

@Composable
private fun GenericMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiary
    )
}

internal val chatHistory = listOf(
    ChatItem.Generic("Generic message"),
    ChatItem.Message(1, 2, false, "Name 1", "This is the message"),
    ChatItem.Message(0, 3, true, "Name 1", "This is the response"),
    ChatItem.Message(0, 3, true, "Name 1", "This is the response"),
    ChatItem.Server(-1, "The server message"),
    ChatItem.Message(0, 3, true, "Name 1", "This is the response"),
    ChatItem.Message(1, 2, false, "Name 1", "This is the message"),
    ChatItem.Message(1, 2, false, "Name 1", "This is the message"),
    ChatItem.Message(1, -1, false, "Name 1", "Unspecified"),
)

@Composable
@Previews
private fun Preview() {
    AppTheme {
        ChatList(chatHistory, GameMode.GAMEMODE_4_COLORS_4_PLAYERS)
    }
}