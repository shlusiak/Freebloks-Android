package de.saschahlusiak.freebloks.app

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

val defaultTypography = Typography()

val tabletTypography = Typography(
    displayLarge = defaultTypography.displayLarge, // 57
    displayMedium = defaultTypography.displayMedium, // 45
    displaySmall = defaultTypography.displaySmall, // 36

    headlineLarge = defaultTypography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 42.sp), // 32
    headlineMedium = defaultTypography.headlineMedium.copy(fontSize = 32.sp, lineHeight = 38.sp), // 28
    headlineSmall = defaultTypography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 34.sp), // 24

    titleLarge = defaultTypography.titleLarge.copy(fontSize = 26.sp, lineHeight = 32.sp), // 22
    titleMedium = defaultTypography.titleMedium.copy(fontSize = 20.sp, lineHeight = 36.sp), // 16
    titleSmall = defaultTypography.titleSmall.copy(fontSize = 18.sp, lineHeight = 24.sp), // 14

    bodyLarge = defaultTypography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 24.sp), // 16
    bodyMedium = defaultTypography.bodyMedium.copy(fontSize = 17.sp, lineHeight = 22.sp), // 14
    bodySmall = defaultTypography.bodySmall.copy(fontSize = 16.sp, lineHeight = 21.sp), // 12

    labelLarge = defaultTypography.labelLarge.copy(fontSize = 17.sp, lineHeight = 23.sp), // 14
    labelMedium = defaultTypography.labelMedium.copy(fontSize = 16.sp, lineHeight = 20.sp), // 12
    labelSmall = defaultTypography.labelSmall.copy(fontSize = 15.sp, lineHeight = 19.sp), // 11
)