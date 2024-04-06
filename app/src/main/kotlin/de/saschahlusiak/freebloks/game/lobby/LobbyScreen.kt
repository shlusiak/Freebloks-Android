package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.game.newgame.GameTypeRow
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun LobbyScreen(
    isRunning: Boolean,
    status: MessageServerStatus?,
    players: List<PlayerColor>,
    chatHistory: List<ChatItem>,
    onGameMode: (GameMode) -> Unit,
    onSize: (Int) -> Unit,
    onTogglePlayer: (Int) -> Unit,
    onChat: (String) -> Unit,
    onStart: () -> Unit,
    onDisconnect: () -> Unit
) {
    Dialog {
        Column(
            Modifier
                .padding(dimensionResource(id = R.dimen.dialog_padding))
                .fillMaxWidth(),
            verticalArrangement = spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = if (isRunning) R.string.chat else R.string.waiting_for_players),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            GameTypeRow(
                modifier = Modifier.padding(top = 8.dp),
                gameMode = status?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                size = status?.width ?: Board.DEFAULT_BOARD_SIZE,
                enabled = (status != null) && !isRunning,
                onGameMode = onGameMode,
                onSize = onSize
            )

            if (status != null) {
                PlayersRow(isRunning, players, onTogglePlayer)
            }

            ChatList(
                chatHistory,
                mode = status?.gameMode ?: GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                modifier = Modifier
                    .heightIn(max = 240.dp)
                    .fillMaxHeight()
                    .fillMaxWidth()
            )

            ChatTextField(onChat = onChat)

            if (!isRunning) {
                Row(horizontalArrangement = spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }

                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        enabled = (status != null && status.player >= 1 && status.clients > 1) || BuildConfig.DEBUG
                    ) {
                        Text(stringResource(id = R.string.start))
                    }
                }
            }
        }
    }
}

internal val previewStatus = MessageServerStatus(
    player = 0,
    computer = 1,
    clients = 3,
    width = 20,
    height = 20,
    gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
    clientForPlayer = arrayOf(1, 0, 0, 0),
    clientNames = arrayOf("Client 1", "Client 2", null, null, null, null, null, null)
)

@Composable
@Previews
private fun Preview() {
    AppTheme {
        LobbyScreen(
            isRunning = false,
            chatHistory = chatHistory,
            status = previewStatus,
            players = listOf(
                PlayerColor(0, StoneColor.Blue, 1, "Sascha", true),
                PlayerColor(1, StoneColor.Yellow, 2, "Paul", false),
                PlayerColor(2, StoneColor.Red, 3, null, false),
                PlayerColor(3, StoneColor.Green, null, null, false),
            ),
            onGameMode = {},
            onSize = {},
            onTogglePlayer = {},
            onChat = {},
            onStart = {},
            onDisconnect = {}
        )
    }
}