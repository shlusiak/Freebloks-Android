package de.saschahlusiak.freebloks.theme

import android.graphics.Color
import de.saschahlusiak.freebloks.R

/**
 * Some plain color themes
 */
object ColorThemes {
    val Green = ColorTheme("green", label = R.string.theme_green, r = 0, g = 64, b = 0)
    val Blue = ColorTheme("blue", label = R.string.theme_blue, colorRes = R.color.theme_background_blue)

    // only added in Debug builds
    val Black = ColorTheme("black", label = R.string.theme_black, color = Color.BLACK)
    val White = ColorTheme("white", label = R.string.theme_white, color = Color.WHITE)
}
