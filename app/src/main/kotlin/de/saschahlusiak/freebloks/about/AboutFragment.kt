package de.saschahlusiak.freebloks.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.donate.DonateFragment

class AboutFragment : DialogFragment() {
    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)

        view as ComposeView
        view.setContent {
            AppTheme {
                AboutScreen(
                    onLink = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) },
                    onDonate = { DonateFragment().show(parentFragmentManager, null) },
                    onDismiss = { dismiss() }
                )
            }
        }
    }
}