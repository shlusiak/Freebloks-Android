package de.saschahlusiak.freebloks.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

internal val DefaultTypography = Typography()

internal val TabletTypography = Typography(
    displayLarge = DefaultTypography.displayLarge, // 57
    displayMedium = DefaultTypography.displayMedium, // 45
    displaySmall = DefaultTypography.displaySmall, // 36

    headlineLarge = DefaultTypography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 40.sp), // 32
    headlineMedium = DefaultTypography.headlineMedium.copy(fontSize = 32.sp, lineHeight = 36.sp), // 28
    headlineSmall = DefaultTypography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 34.sp), // 24

    titleLarge = DefaultTypography.titleLarge.copy(fontSize = 26.sp, lineHeight = 32.sp), // 22
    titleMedium = DefaultTypography.titleMedium.copy(fontSize = 20.sp, lineHeight = 26.sp), // 16
    titleSmall = DefaultTypography.titleSmall.copy(fontSize = 18.sp, lineHeight = 22.sp), // 14

    bodyLarge = DefaultTypography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 22.sp), // 16
    bodyMedium = DefaultTypography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 21.sp), // 14
    bodySmall = DefaultTypography.bodySmall.copy(fontSize = 16.sp, lineHeight = 20.sp), // 12

    labelLarge = DefaultTypography.labelLarge.copy(fontSize = 18.sp, lineHeight = 23.sp), // 14
    labelMedium = DefaultTypography.labelMedium.copy(fontSize = 16.sp, lineHeight = 20.sp), // 12
    labelSmall = DefaultTypography.labelSmall.copy(fontSize = 15.sp, lineHeight = 19.sp), // 11
)