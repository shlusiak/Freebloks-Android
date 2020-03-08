package de.saschahlusiak.freebloks.theme

import android.graphics.Color
import de.saschahlusiak.freebloks.R

/**
 * Some plain color themes
 */
@Suppress("DEPRECATION")
object ColorThemes {
    val Green = ColorTheme("GREEN", label = R.string.theme_green, r = 0, g = 64, b = 0)
    val Blue = ColorTheme("BLUE", label = R.string.theme_blue, r = 0, g = 55, b = 104)

    // only added in Debug builds
    val Black = ColorTheme("BLACK", label = R.string.theme_black, color = Color.BLACK)
    val White = ColorTheme("WHITE", label = R.string.theme_white, color = Color.WHITE)
}
