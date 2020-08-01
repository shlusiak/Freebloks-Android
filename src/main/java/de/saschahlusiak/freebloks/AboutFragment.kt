package de.saschahlusiak.freebloks

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import kotlinx.android.synthetic.main.about_activity.*

class AboutFragment : MaterialDialogFragment() {

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.about_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ok.setOnClickListener { dismiss() }
        version.text = BuildConfig.VERSION_NAME
        url.text = Global.getMarketURLString(BuildConfig.APPLICATION_ID)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.about_freebloks)
        }
    }
}