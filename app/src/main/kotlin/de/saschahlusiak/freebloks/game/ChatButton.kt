package de.saschahlusiak.freebloks.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.app.theme.pillButtonBackground
import de.saschahlusiak.freebloks.utils.Previews

@Composable
fun ChatButton(unread: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val containerColor = if (unread > 0) MaterialTheme.colorScheme.primaryContainer else pillButtonBackground
    val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor).takeOrElse { Color.White }

    Box(
        modifier = modifier
            .wrapContentSize()
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Min)
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = "")
        }

        AnimatedVisibility(
            visible = unread > 0,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .offset(x = 5.dp, y = 5.dp)
                    .sizeIn(22.dp, 22.dp),
                shape = CircleShape
            ) {
                Text(
                    text = unread.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}

@Previews
@Composable
private fun ChatButtonPreview() {
    AppTheme {
        ChatButton(modifier = Modifier.padding(8.dp), unread = 5) {}
    }
}
