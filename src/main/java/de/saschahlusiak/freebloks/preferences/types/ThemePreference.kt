package de.saschahlusiak.freebloks.preferences.types

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.theme.ThemeManager

class ThemePreference(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs) {
    init {
        val tm = ThemeManager.get(context)
        val themes = tm.getAllThemes()

        // TODO: support new themes
        /*
        val entries = arrayOfNulls<String>(themes.size)
        val values = arrayOfNulls<String>(themes.size)

        for (i in themes.indices) {
            val theme = themes[i]

            entries[i] = theme.getLabel(context)
            values[i] = theme.name
        }
    */

        this.entries = context.resources.getStringArray(R.array.theme_labels)
        this.entryValues = context.resources.getStringArray(R.array.theme_values)
    }
}
