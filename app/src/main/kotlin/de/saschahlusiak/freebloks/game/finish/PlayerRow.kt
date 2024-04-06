package de.saschahlusiak.freebloks.game.finish

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.isPopupLayout
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.PlayerScore
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun PlayerRow(
    modifier: Modifier,
    gameMode: GameMode,
    score: PlayerScore
) {
    val color = gameMode.colorOf(score.color1)
    val color2 = gameMode.colorOf(score.color2).takeIf { score.color2 >= 0 }
    val isLocal = score.isLocal

    val infiniteTransition = rememberInfiniteTransition(label = "jump animation")
    val animationOffset = with(LocalDensity.current) { 12.dp.toPx() }
    val translation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = animationOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
    ) {
        Surface(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary).takeIf { isLocal },
            color = if (isLocal) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .aspectRatio(1f)
        ) {
            Box {
                Text(
                    text = "${score.place}.",
                    color = if (isLocal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f),
        ) {
            if (color2 != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 8.dp, y = 5.dp),
                    shadowElevation = 5.dp,
                    shape = RoundedCornerShape(4.dp),
                    color = colorResource(id = color2.backgroundColorId),
                    contentColor = Color.White
                ) { }
            }

            Surface(
                shadowElevation = 3.dp,
                shape = RoundedCornerShape(4.dp),
                color = colorResource(id = color.backgroundColorId),
                contentColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Row(
                        Modifier.align(Alignment.CenterStart)
                    ) {
                        if (score.isPerfect) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_star), contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        Text(
                            text = score.clientName ?: stringResource(id = color.labelResId),
                            modifier = Modifier
                                .graphicsLayer {
                                    if (isLocal) {
                                        translationX = translation
                                    }
                                },
                            style = MaterialTheme.typography.bodyMedium.merge(shadow),
                            fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (score.isPerfect) {
                                Text(
                                    "(+${score.bonus})",
                                    modifier = Modifier
                                        .padding(end = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xaf, 0xf7, 0xaf),
                                )
                            }

                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.number_of_points,
                                    count = score.totalPoints,
                                    score.totalPoints
                                ),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge.merge(shadow)
                            )
                        }

                        Text(
                            pluralStringResource(
                                id = R.plurals.number_of_stones_left,
                                count = score.stonesLeft,
                                score.stonesLeft
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

private val shadow = TextStyle(
    shadow = Shadow(
        Color.DarkGray,
        offset = Offset(0.6f, 1.5f),
        blurRadius = 5f
    )
)

@Composable
@Previews
private fun Preview() {
    AppTheme {
        Surface {
            PlayerRow(
                modifier = Modifier,
                gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                score = PlayerScore(
                    color1 = 0,
                    color2 = -1,
                    totalPoints = 19,
                    stonesLeft = 3,
                    turnsLeft = 1,
                    bonus = 15,
                    isPerfect = true,
                    clientName = "Sascha",
                    place = 1,
                    isLocal = true
                )
            )
        }
    }
}

@Composable
@Previews
private fun PreviewMultiple() {
    AppTheme {
        Surface {
            PlayerRow(
                modifier = Modifier,
                gameMode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS,
                score = PlayerScore(
                    color1 = 1,
                    color2 = 3,
                    totalPoints = 19,
                    stonesLeft = 3,
                    turnsLeft = 1,
                    bonus = 15,
                    isPerfect = false,
                    clientName = null,
                    place = 2,
                    isLocal = false
                )
            )
        }
    }
}