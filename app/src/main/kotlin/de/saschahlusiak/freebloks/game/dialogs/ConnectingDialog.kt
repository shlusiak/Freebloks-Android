package de.saschahlusiak.freebloks.game.dialogs

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.utils.Dialog
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.MaterialProgressDialog

/**
 * Simple dialog fragment to show "Connecting to server..." that disconnects the client on cancel.
 */
class ConnectingDialog : MaterialDialogFragment(null) {
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }

    override fun getTheme() = R.style.Theme_Freebloks_Dialog

    override fun onCancel(dialog: DialogInterface) {
        viewModel.disconnectClient()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (Feature.COMPOSE) {
            return MaterialDialog(requireContext(), theme, false)
        }

        @Suppress("DEPRECATION")
        return MaterialProgressDialog(requireContext(), 0).apply {
            setMessage(getString(R.string.connecting_to_server))
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel)) { d, _ -> d.cancel() }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (Feature.COMPOSE) {
            ComposeView(requireContext())
        } else
            super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (view is ComposeView) {
            dialog?.window?.setBackgroundDrawable(null)
            view.setContent {
                DialogContent()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    @Composable
    private fun DialogContent() {
        Dialog {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(id = R.string.connecting_to_server))
                }

                TextButton(
                    modifier = Modifier.align(alignment = Alignment.End),
                    onClick = { dialog?.cancel() }
                ) {
                    Text(stringResource(id = android.R.string.cancel))
                }
            }
        }
    }

    @Composable
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
    @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "DE")
    private fun Preview() {
        AppTheme {
            DialogContent()
        }
    }
}

