package de.saschahlusiak.freebloks.utils

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import de.saschahlusiak.freebloks.googleServices.BuildConfig
import io.fabric.sdk.android.Fabric

class CrashlyticsCrashReporter(context: Context) : CrashReporter() {
    init {
        val crashlyticsKit: Crashlytics = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
            .build()

        Fabric.with(context, crashlyticsKit)
    }

    override fun log(priority: Int, tag: String, message: String) {
        Crashlytics.log(priority, tag, message)
    }

    override fun logException(e: Exception) {
        Crashlytics.logException(e)
    }

    override fun setString(key: String, value: String) {
        Crashlytics.setString(key, value)
    }
}