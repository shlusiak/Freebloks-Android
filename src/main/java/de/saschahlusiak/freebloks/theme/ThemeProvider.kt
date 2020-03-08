package de.saschahlusiak.freebloks.theme

import android.content.Context

import androidx.annotation.Keep

@Keep
interface ThemeProvider {
    @Keep
    fun getAllThemes(context: Context): Collection<Theme>
}
