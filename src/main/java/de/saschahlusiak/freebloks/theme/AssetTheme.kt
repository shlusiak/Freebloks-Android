package de.saschahlusiak.freebloks.theme

import androidx.annotation.DrawableRes

class AssetTheme(
    label: Int,
    @DrawableRes preview: Int,
    private val assetName: String,
    override val name: String = assetName,
    override val ratio: Float = 1.0f
) : BaseTheme(label, preview) {
    
    override val asset: String? get() = "textures/$assetName.ktx"
    override val isResource: Boolean
        get() = true
}
