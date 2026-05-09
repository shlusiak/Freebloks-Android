package de.saschahlusiak.freebloks.preferences.types

import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.theme.ThemeManager
import javax.inject.Inject

@AndroidEntryPoint
class ThemePreferenceDialogFragment : ListPreferenceDialogFragment() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        val preference = preference as ListPreference
        val values = preference.entryValues

        val checked = values.indexOf(preference.value)

        val adapter = ThemePreferenceAdapter(builder.context, themeManager, checked).apply {
            addAll(values.toList())
        }
        builder.setAdapter(adapter) { dialog, which ->
            val value = values[which].toString()
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
            dialog.dismiss()
        }
    }
}