package de.saschahlusiak.freebloks.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle

/**
 * A ProgressDialog with material style background
 */
@Deprecated("Delete")
class MaterialProgressDialog(context: Context, theme: Int, val apply: Boolean = true) : ProgressDialog(context, theme) {
    @Deprecated("Deprecated in Java")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (apply) {
            applyMaterialBackground()
        }
    }
}
