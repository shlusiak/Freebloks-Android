package de.saschahlusiak.freebloks.app.theme

import android.os.Build.VERSION
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.Feature.FORCE_TABLET_DIMENSIONS

@Composable
fun isTablet(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.smallestScreenWidthDp >= 600 || FORCE_TABLET_DIMENSIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isTablet: Boolean = isTablet(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = if (VERSION.SDK_INT >= 31 && Feature.DYNAMIC_COLORS) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) DarkColors else LightColors
    }

    val typography = if (isTablet) TabletTypography else DefaultTypography
    
    val dimensions = if (isTablet) TabletDimensions else DefaultDimensions

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentEnforcement provides false,
        LocalDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}