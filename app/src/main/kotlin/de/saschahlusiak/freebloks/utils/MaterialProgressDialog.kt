package de.saschahlusiak.freebloks.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle

/**
 * A ProgressDialog with material style background
 */
@Suppress("DEPRECATION")
class MaterialProgressDialog(context: Context, theme: Int) : ProgressDialog(context, theme) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyMaterialBackground()
    }
}
