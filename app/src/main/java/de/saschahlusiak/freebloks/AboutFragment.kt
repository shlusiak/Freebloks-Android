package de.saschahlusiak.freebloks

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import de.saschahlusiak.freebloks.databinding.AboutActivityBinding
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.viewBinding

class AboutFragment : MaterialDialogFragment(R.layout.about_activity) {

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    private val binding: AboutActivityBinding by viewBinding(AboutActivityBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            ok.setOnClickListener { dismiss() }
            version.text = BuildConfig.VERSION_NAME
            url.text = Global.getMarketURLString(BuildConfig.APPLICATION_ID)

            donate.setOnClickListener {
                startActivity(Intent(requireContext(), DonateActivity::class.java))
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.about_freebloks)
        }
    }
}