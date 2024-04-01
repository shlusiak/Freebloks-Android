package de.saschahlusiak.freebloks.game.lobby

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.game.newgame.GameTypeRow
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun LobbyScreen(
    @StringRes title: Int,
    status: MessageServerStatus?,
    chatHistory: List<ChatItem>,
    onGameMode: (GameMode) -> Unit,
    onSize: (Int) -> Unit,
    onChat: (String) -> Unit,
    onStart: () -> Unit
) {
    Dialog {
        Column(
            Modifier
                .padding(dimensionResource(id = R.dimen.dialog_padding))
                .fillMaxWidth(),
            verticalArrangement = spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            GameTypeRow(
                modifier = Modifier.padding(top = 8.dp),
                gameMode = status?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                size = status?.width ?: Board.DEFAULT_BOARD_SIZE,
                enabled = (status != null),
                onGameMode = onGameMode,
                onSize = onSize
            )

            ChatList(
                chatHistory,
                mode = status?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                modifier = Modifier
                    .heightIn(max = 260.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )

            ChatTextField(onChat = onChat)

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                enabled = status != null && status.clients > 1
            ) {
                Text(stringResource(id = R.string.start))
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        LobbyScreen(
            title = R.string.chat,
            chatHistory = chatHistory,
            status = MessageServerStatus(
                player = 0,
                computer = 1,
                clients = 3,
                width = 20,
                height = 20,
                gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                clientForPlayer = arrayOf(1, 0, 0, 0),
                clientNames = Array(8) { null }
            ),
            onGameMode = {},
            onSize = {},
            onChat = {},
            onStart = {}
        )
    }
}