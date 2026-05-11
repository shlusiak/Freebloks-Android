package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
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

    @Composable
    override fun brush(): Brush {
        return if (isResource) {
            val bitmap = ImageBitmap.imageResource(preview)

            object: ShaderBrush() {
                override fun createShader(size: Size): androidx.compose.ui.graphics.Shader {
                    return BitmapShader(
                        bitmap.asAndroidBitmap(),
                        Shader.TileMode.REPEAT,
                        Shader.TileMode.MIRROR
                    ).apply {
                        val scale = size.height / bitmap.height
                        val matrix = Matrix().apply {
                            setScale(scale, scale)
                        }
                        setLocalMatrix(matrix)
                    }
                }
            }
        } else {
            SolidColor(Color(getColor(LocalResources.current)))
        }
    }

    override fun toString(): String = name
}
