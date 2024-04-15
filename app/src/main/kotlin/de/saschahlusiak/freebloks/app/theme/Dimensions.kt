package de.saschahlusiak.freebloks.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimensions(
    val activityPadding: Dp = 16.dp,
    val dialogPadding: Dp = 16.dp,
    val dialogCornerRadius: Dp = 12.dp,
    val buttonSize: Dp = 44.dp,

    val mainMenuPadding: Dp = 16.dp,
    val mainMenuButtonMargin: Dp = 10.dp,
    val mainMenuButtonHeight: Dp = 52.dp,

    val innerPaddingSmall: Dp = 4.dp,
    val innerPaddingMedium: Dp = 8.dp,
    val innerPaddingLarge: Dp = 12.dp
)

val DefaultDimensions = Dimensions()

val TabletDimensions = Dimensions(
    activityPadding = 20.dp,
    dialogPadding = 20.dp,
    dialogCornerRadius = 16.dp,
    buttonSize = 52.dp,
    mainMenuPadding = 20.dp,
    mainMenuButtonMargin = 12.dp,
    mainMenuButtonHeight = 64.dp,
    innerPaddingSmall = 6.dp,
    innerPaddingMedium = 10.dp,
    innerPaddingLarge = 14.dp
)

internal val LocalDimensions = staticCompositionLocalOf { Dimensions() }

val MaterialTheme.dimensions: Dimensions @Composable get() = LocalDimensions.current