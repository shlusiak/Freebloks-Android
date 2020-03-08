package de.saschahlusiak.freebloks.preferences.types

import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import de.saschahlusiak.wordmix.preferences.types.ListPreferenceDialogFragment

class ThemePreferenceDialogFragment : ListPreferenceDialogFragment() {
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        val preference = preference as ListPreference
        val values = preference.entryValues

        val adapter = ThemePreferenceAdapter(builder.context).apply {
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