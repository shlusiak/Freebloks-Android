package de.saschahlusiak.freebloks.utils

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment

@Deprecated("Delete")
open class MaterialDialogFragment(@LayoutRes val layoutResId: Int?) : AppCompatDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (layoutResId == null) {
            super.onCreateView(inflater, container, savedInstanceState)
        } else {
            inflater.inflate(layoutResId, container, false)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext(), theme)
    }
}