@file:Suppress("DEPRECATION")

package de.saschahlusiak.freebloks.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle

/**
 * A ProgressDialog with material style background
 */
class MaterialProgressDialog(context: Context) : ProgressDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyMaterialBackground()
    }
}
