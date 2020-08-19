package de.saschahlusiak.freebloks.preferences.types

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.ThemeManager

class ThemePreferenceAdapter(context: Context, private val checkedPosition: Int) : ArrayAdapter<CharSequence>(context, 0) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val themeManager = ThemeManager.get(context)

    override fun hasStableIds() = true

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: inflater.inflate(R.layout.theme_preference_list_item, parent, false)

        val themeName = getItem(position).toString()

        val theme = themeManager.getTheme(themeName, ColorThemes.Black)
        v.background = theme.getPreview(v.resources)

        v.findViewById<CheckedTextView>(android.R.id.text1).isChecked = (position == checkedPosition)

        return v
    }
}