package de.saschahlusiak.freebloks.theme

import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes

/**
 * Simple plain color theme, either defined via a color resource or a color value
 *
 * @param name unique key for this theme
 * @param label the label string resource id
 * @param colorRes color resource, or 0
 * @param color color value, if [colorRes] is 0
 */
class ColorTheme(override val name: String, @StringRes label: Int, @ColorRes val colorRes: Int = 0, @ColorInt val color: Int = 0)
    : BaseTheme(label = label) {

    constructor(name: String, @StringRes label: Int, r: Int, g: Int, b: Int): this(
        name = name,
        label = label,
        color = Color.rgb(r, g, b)
    )

    override fun getColor(resources: Resources): Int {
        return if (colorRes != 0) {
            resources.getColor(colorRes)
        } else {
            color
        }
    }
}
