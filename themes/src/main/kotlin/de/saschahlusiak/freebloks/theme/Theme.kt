package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable

/**
 * Interface definition of a theme
 */
interface Theme {

    /**
     * @return true if this is an asset, false if this is a color
     */
    val isResource: Boolean

    /**
     * @return a name for the theme to uniquely identify it in the preferences
     */
    val name: String

    /**
     * @return the path name of a ktx texture for the asset, if resource is true
     */
    val asset: String?

    /**
     * @return for assets, the aspect ratio, 1.0 for default
     */
    val ratio: Float

    /**
     * @param context Context
     * @return display string for this theme
     */
    fun getLabel(context: Context): String

    /**
     * @param resources resources
     * @return the color int, in case this is not a resource
     */
    fun getColor(resources: Resources): Int

    /**
     * @param resources Resources
     * @return a drawable for the preview to show in the theme selector
     */
    fun getPreview(resources: Resources): Drawable

    /**
     * Returns the solid color in float, if resource is false
     *
     * @param resources the context resources
     * @param rgb output float array to receive rgb, but not a
     */
    fun getColor(resources: Resources, rgb: FloatArray) {
        val color = getColor(resources)
        rgb[0] = Color.red(color).toFloat() / 255.0f
        rgb[1] = Color.green(color).toFloat() / 255.0f
        rgb[2] = Color.blue(color).toFloat() / 255.0f
    }
}

