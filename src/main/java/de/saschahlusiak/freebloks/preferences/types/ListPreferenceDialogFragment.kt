package de.saschahlusiak.wordmix.preferences.types

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.ListPreferenceDialogFragmentCompat
import androidx.preference.PreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

open class ListPreferenceDialogFragment : ListPreferenceDialogFragmentCompat() {
    // TODO: support material design
    /*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // exactly the same as super.onCreateDialog, but uses a MaterialAlertDialogBuilder instead
        val context: Context? = activity
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(preference.dialogTitle)
            .setIcon(preference.dialogIcon)
            .setPositiveButton(preference.positiveButtonText, this)
            .setNegativeButton(preference.negativeButtonText, this)
        val contentView = onCreateDialogView(context)
        if (contentView != null) {
            onBindDialogView(contentView)
            builder.setView(contentView)
        } else {
            builder.setMessage(preference.dialogMessage)
        }
        onPrepareDialogBuilder(builder)
        return builder.create()
    }
    */

    fun setKey(key: String): ListPreferenceDialogFragment {
        if (arguments == null) arguments = Bundle()
        arguments?.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
        return this
    }
}