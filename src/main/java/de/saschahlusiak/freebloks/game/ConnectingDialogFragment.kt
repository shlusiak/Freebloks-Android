package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.saschahlusiak.freebloks.R

/**
 * Simple dialog fragment to show "Connecting to server..." that disconnects the client on cancel.
 */
class ConnectingDialogFragment : DialogFragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(FreebloksActivityViewModel::class.java) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ProgressDialog(context);
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        if (dialog is ProgressDialog) {
            dialog.setMessage(getString(R.string.connecting))
            dialog.setIndeterminate(true)
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            dialog.setCancelable(true)
            dialog.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel)) { d, _ ->
                viewModel.disconnectClient()
            }
        }
    }
}