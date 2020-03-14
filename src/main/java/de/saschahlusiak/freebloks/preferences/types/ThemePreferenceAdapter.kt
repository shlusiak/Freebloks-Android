package de.saschahlusiak.freebloks.preferences.types

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.view.scene.LegacyTheme

class ThemePreferenceAdapter(context: Context) : ArrayAdapter<CharSequence>(context, 0) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    // TODO: support new ThemeManager
//    private val themeManager = ThemeManager.get(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: inflater.inflate(R.layout.theme_preference_list_item, parent, false)

        val themeName = getItem(position).toString()

//        val theme = themeManager.getTheme(themeName, null)
//        v.background = theme?.getPreview(v.resources)

        val t = LegacyTheme.getLegacy(context, themeName)
        t?.apply(v)

        return v
    }
}