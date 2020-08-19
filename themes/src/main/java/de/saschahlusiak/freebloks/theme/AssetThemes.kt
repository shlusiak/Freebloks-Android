package de.saschahlusiak.freebloks.theme

import android.content.Context
import android.graphics.Color

@Suppress("PrivatePropertyName")
class AssetThemes : ThemeProvider {
    private val FloweryCloth = AssetTheme(
        R.string.theme_flowery_cloth,
        R.drawable.texture_table_1_preview,
        "texture_table_1",
        "texture_table_cloth_1"
    )

    private val StripedCloth = AssetTheme(
        R.string.theme_striped_cloth,
        R.drawable.texture_table_2_preview,
        "texture_table_2",
        "texture_table_cloth_2"
    )

    private val Wood = AssetTheme(
        R.string.theme_wood,
        R.drawable.texture_wood_fine_preview,
        "texture_wood_fine",
        "texture_wood"
    )

    private val Metal = AssetTheme(
        R.string.theme_metal,
        R.drawable.texture_metal_preview,
        "texture_metal"
    )

    private val Bricks = AssetTheme(
        R.string.theme_bricks,
        R.drawable.texture_bricks_preview,
        "texture_bricks"
    )

    private val Carpet = AssetTheme(
        R.string.theme_carpet,
        R.drawable.texture_carpet_blue_preview,
        "texture_carpet_blue"
    )

    private val Velvet = AssetTheme(
        R.string.theme_velvet,
        R.drawable.texture_velvet_preview,
        "texture_velvet"
    )

    private val Grass = AssetTheme(
        R.string.theme_grass,
        R.drawable.texture_grass_preview,
        "texture_grass"
    )

    override fun getAllThemes(context: Context): Collection<Theme> {
        return listOf(
            FloweryCloth,
            StripedCloth,
            Wood,
            Metal,
            Bricks,
            Carpet,
            Velvet,
            Grass
        )
    }
}

@Suppress("PrivatePropertyName")
class BoardThemes : ThemeProvider {
    private val LightGrey = ColorTheme(
        "grey",
        label = R.string.theme_grey,
        color = Color.rgb(145, 145, 140)
    )

    private val Wood = AssetTheme(
        R.string.theme_wood,
        R.drawable.field_wood_preview,
        "field_wood",
        color = Color.rgb(145, 145, 145)
    )

    override fun getAllThemes(context: Context): Collection<Theme> {
        return listOf(
            LightGrey,
            Wood
        )
    }
}