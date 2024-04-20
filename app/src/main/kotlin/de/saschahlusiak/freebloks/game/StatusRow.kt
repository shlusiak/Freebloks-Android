package de.saschahlusiak.freebloks.game

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.dimensions
import de.saschahlusiak.freebloks.app.theme.playerColorOnBackground
import de.saschahlusiak.freebloks.app.theme.playerColorOnForeground
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.utils.Previews

val neutralBackground = Color(64, 64, 80)

@Immutable
sealed interface StatusData {
    data class Text(@StringRes val res: Int) : StatusData

    data class Player(
        val isRotated: Boolean,
        val isYourTurn: Boolean,
        val name: String,
        val currentPlayer: StoneColor,
        val points: Int,
        val movesLeft: Int,
        val stonesLeft: Int,
        val inProgress: Boolean
    ) : StatusData
}

@Composable
fun StatusRow(vm: FreebloksActivityViewModel) {
    val sheetPlayer = vm.playerToShowInSheet.collectAsState().value
    val inProgress = vm.inProgress.collectAsState().value

    val client = vm.client

    val data = when {
        // the intro trumps everything
        vm.intro != null -> StatusData.Text(R.string.touch_to_skip)

        // if not connected, show that
        client == null || !client.isConnected() -> StatusData.Text(R.string.not_connected)

        // no current player
        sheetPlayer.showPlayer < 0 -> StatusData.Text(R.string.no_player)

        else -> {
            val game = client.game
            val board = game.board

            // is it "your turn", like, in general?
            val isYourTurn = client.game.isLocalPlayer()

            val playerColor = game.gameMode.colorOf(sheetPlayer.showPlayer)

            val playerName = vm.getPlayerName(sheetPlayer.showPlayer)
            val p = board.getPlayer(sheetPlayer.showPlayer)

            StatusData.Player(
                isRotated = sheetPlayer.isRotated,
                isYourTurn = isYourTurn,
                name = playerName,
                currentPlayer = playerColor,
                points = p.totalPoints,
                movesLeft = p.numberOfPossibleTurns,
                stonesLeft = p.stonesLeft,
                inProgress = !isYourTurn || inProgress
            )
        }
    }

    StatusRow(data)
}

private val shadow = TextStyle(
    shadow = Shadow(
        color = Color.Black,
        offset = Offset(0.6f, 1.3f),
        blurRadius = 5f
    )
)

@Composable
fun revealBrush(color: Color): Brush {
    val screenWidth = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    var previous by remember { mutableStateOf(color) }
    var current by remember { mutableStateOf(color) }
    val radius = remember { Animatable(1f) }

    LaunchedEffect(key1 = color) {
        radius.snapTo(1f)
        current = color
        radius.animateTo(
            screenWidth,
            tween(
                durationMillis = 400,
                easing = LinearEasing
            )
        )
        previous = color
    }

    return Brush.radialGradient(
        0f to current,
        0.98f to current,
        1f to previous,
        radius = radius.value,
        center = Offset(x = screenWidth / 2f, y = Float.POSITIVE_INFINITY)
    )
}


@Composable
fun StatusRow(data: StatusData) {
    val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

    val (color, contentColor) = when (data) {
        is StatusData.Player -> if (!data.isRotated && data.isYourTurn)
            data.currentPlayer.backgroundColor to Color.White
        else data.currentPlayer.backgroundColor to Color.White

        else -> neutralBackground to Color.White
    }

    val brush = revealBrush(color = color)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush)
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .padding(
                vertical = 8.dp,
                horizontal = MaterialTheme.dimensions.innerPaddingSmall
            )
    ) {
        when (data) {
            is StatusData.Text -> {
                Text(
                    text = stringResource(id = data.res),
                    modifier = Modifier.align(alignment = Center),
                    style = MaterialTheme.typography.titleMedium
                        .merge(shadow),
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            is StatusData.Player -> {
                Text(
                    text = pluralStringResource(id = R.plurals.number_of_points, data.points, data.points),
                    modifier = Modifier.align(alignment = CenterStart),
                    color = contentColor,
                    style = MaterialTheme.typography.labelMedium
                        .merge(shadow)
                )

                // we are showing "home"
                val text = if (data.movesLeft <= 0) {
                    data.name
                } else if (!data.isRotated) {
                    if (data.isYourTurn) {
                        stringResource(id = R.string.your_turn, data.name)
                    } else {
                        stringResource(R.string.waiting_for_color, data.name)
                    }
                } else {
                    data.name
                }

                // center text
                Text(
                    text = text,
                    modifier = Modifier.align(alignment = Center),
                    style = MaterialTheme.typography.titleMedium
                        .merge(shadow),
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = when {
                        data.movesLeft <= 0 -> TextDecoration.LineThrough
                        else -> null
                    }
                )

                if (data.movesLeft <= 0) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.number_of_stones_left,
                            data.stonesLeft,
                            data.stonesLeft
                        ),
                        modifier = Modifier.align(alignment = CenterEnd),
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall
                            .merge(shadow)
                    )
                } else if (data.inProgress) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 1.8.dp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                            .align(CenterEnd)
                            .alpha(0.85f)
                    )
                } else {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.player_status_moves,
                            data.movesLeft,
                            data.movesLeft
                        ),
                        color = contentColor,
                        modifier = Modifier.align(alignment = CenterEnd),
                        style = MaterialTheme.typography.labelMedium
                            .merge(shadow)
                    )
                }
            }
        }
    }
}

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Column {
            StatusRow(data = StatusData.Text(R.string.touch_to_skip))

            StatusRow(
                StatusData.Player(
                    isRotated = false,
                    isYourTurn = true,
                    name = "Sascha",
                    currentPlayer = StoneColor.Blue,
                    points = 17,
                    movesLeft = 516,
                    stonesLeft = 2,
                    inProgress = true
                )
            )

            StatusRow(
                StatusData.Player(
                    isRotated = false,
                    isYourTurn = false,
                    name = "Red",
                    currentPlayer = StoneColor.Red,
                    points = 18,
                    movesLeft = 516,
                    stonesLeft = 3,
                    inProgress = false
                )
            )
            StatusRow(
                StatusData.Player(
                    isRotated = true,
                    isYourTurn = true,
                    name = "Orange",
                    currentPlayer = StoneColor.Orange,
                    points = 0,
                    movesLeft = 1,
                    stonesLeft = 1,
                    inProgress = false
                )
            )
            StatusRow(
                StatusData.Player(
                    isRotated = false,
                    isYourTurn = false,
                    name = "Green",
                    currentPlayer = StoneColor.Green,
                    points = 18,
                    movesLeft = 0,
                    stonesLeft = 3,
                    inProgress = false
                )
            )
            StatusRow(
                StatusData.Player(
                    isRotated = false,
                    isYourTurn = false,
                    name = "Yellow",
                    currentPlayer = StoneColor.Yellow,
                    points = 18,
                    movesLeft = 0,
                    stonesLeft = 8,
                    inProgress = false
                )
            )
        }
    }
}
