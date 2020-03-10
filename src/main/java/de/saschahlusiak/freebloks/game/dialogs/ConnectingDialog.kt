package de.saschahlusiak.freebloks.game.dialogs

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel

/**
 * Simple dialog fragment to show "Connecting to server..." that disconnects the client on cancel.
 */
class ConnectingDialog : AppCompatDialogFragment() {
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    override fun onCancel(dialog: DialogInterface) {
        viewModel.disconnectClient()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return ProgressDialog(context).apply {
            setMessage(getString(R.string.connecting))
            setIndeterminate(true)
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel)) { d, _ -> d.cancel() }
        }
    }
}