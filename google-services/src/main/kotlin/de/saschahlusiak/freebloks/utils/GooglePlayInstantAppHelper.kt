package de.saschahlusiak.freebloks.utils

import android.app.Activity
import android.app.Application
import android.os.Build
import com.google.android.gms.instantapps.InstantApps
import javax.inject.Inject

class GooglePlayInstantAppHandler @Inject constructor(
    private val context: Application
) : InstantAppHelper {
    override val isInstantApp: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.isInstantApp
        } else {
            false
        }

    override fun showInstallPrompt(activity: Activity) {
        InstantApps.showInstallPrompt(
            activity,
            null,
            1,
            null
        )
    }
}