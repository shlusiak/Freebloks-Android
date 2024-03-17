package de.saschahlusiak.freebloks.utils

import android.app.Activity

interface InstantAppHelper {
    val isInstantApp: Boolean

    fun showInstallPrompt(activity: Activity)
}

class DefaultInstantAppHelper: InstantAppHelper {
    override val isInstantApp = false

    override fun showInstallPrompt(activity: Activity) = Unit
}