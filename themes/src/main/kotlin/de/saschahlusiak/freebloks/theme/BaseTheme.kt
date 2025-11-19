package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toDrawable

/**
 * Abstract base class for a theme with default implementations for most methods
 */
abstract class BaseTheme(@StringRes val label: Int, @DrawableRes val preview: Int = 0) : Theme {

    override val isResource: Boolean
        get() = false

    /**
     * The name value of the theme; will be used as a key and stored in preferences
     */
    abstract override val name: String

    override val asset: String?
        get() = null

    override val ratio: Float
        get() = 1.0f

    abstract override fun getColor(resources: Resources): Int

    override fun getLabel(context: Context): String = context.getString(label)

    override fun getPreview(resources: Resources): Drawable {
        if (isResource) {
            val background = resources.getDrawable(preview, null) as BitmapDrawable

            background.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.MIRROR)
            background.isFilterBitmap = true

            return background
        } else {
            return getColor(resources).toDrawable()
        }
    }

    override fun toString(): String = name
}
