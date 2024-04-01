package de.saschahlusiak.freebloks.game.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.sharp.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.utils.Previews

@Immutable
data class PlayerColor(
    val player: Int,
    val color: StoneColor,
    val client: Int?,
    val name: String?,
    val isLocal: Boolean
)

@Composable
fun PlayersRow(
    isRunning: Boolean,
    players: List<PlayerColor>,
    onTogglePlayer: (Int) -> Unit
) {
    Column(
        verticalArrangement = spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        players.chunked(2).forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { player ->
                    val isAvailable = player.client == null || player.isLocal
                    val background = colorResource(id = player.color.backgroundColorId)

                    if (player.client != null) {
                        Button(
                            onClick = { onTogglePlayer(player.player) },
                            enabled = isAvailable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = background,
                                contentColor = Color.White,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(2.dp, color = background),
                            elevation = ButtonDefaults.buttonElevation(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (player.isLocal) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                            Text(
                                text = player.name ?: stringResource(id = R.string.client_d, player.client + 1),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onTogglePlayer(player.player) },
                            enabled = !isRunning,
                            border = BorderStroke(2.dp, color = background),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isRunning) {
                                Text(text = "---")
                            } else {
                                CircularProgressIndicator(
                                    color = colorResource(id = player.color.backgroundColorId),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Surface {
            PlayersRow(
                isRunning = true,
                listOf(
                    PlayerColor(0, StoneColor.Blue, 0, "Sascha", true),
                    PlayerColor(1, StoneColor.Yellow, 0, "Paul", false),
                    PlayerColor(2, StoneColor.Red, 2, null, false),
                    PlayerColor(3, StoneColor.Green, null, null, false),
                )
            ) {}
        }
    }
}