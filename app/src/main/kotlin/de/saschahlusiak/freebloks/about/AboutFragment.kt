package de.saschahlusiak.freebloks.about

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.databinding.AboutActivityBinding
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.viewBinding

class AboutFragment : MaterialDialogFragment(R.layout.about_activity) {

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    private val binding: AboutActivityBinding by viewBinding(AboutActivityBinding::bind)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (Feature.COMPOSE) {
            return ComposeView(requireContext())
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (view is ComposeView) {
            dialog?.window?.setBackgroundDrawable(null)
            view.setContent {
                AppTheme {
                    AboutScreen(
                        onLink = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) },
                        onDonate = { startActivity(Intent(requireContext(), DonateActivity::class.java)) },
                        onDismiss = { dismiss() }
                    )
                }
            }
            return
        }
        with(binding) {
            ok.setOnClickListener { dismiss() }
            version.text = BuildConfig.VERSION_NAME
            url.text = Global.getMarketURLString(BuildConfig.APPLICATION_ID)

            donate.isVisible = !Global.IS_VIP
            donate.setOnClickListener {
                startActivity(Intent(requireContext(), DonateActivity::class.java))
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext(), theme, !Feature.COMPOSE).apply {
            setTitle(R.string.about_freebloks)
        }
    }
}