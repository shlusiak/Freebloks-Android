package de.saschahlusiak.freebloks.theme

import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.DrawableRes

class AssetTheme(
    label: Int,
    @DrawableRes preview: Int,
    private val assetName: String,
    override val name: String = assetName,
    override val ratio: Float = 1.0f,
    private val color: Int = Color.rgb(214, 214, 214)
) : BaseTheme(label, preview) {

    override val asset: String? get() = "textures/$assetName.ktx"
    override val isResource: Boolean
        get() = true

    override fun getColor(resources: Resources) = color
}