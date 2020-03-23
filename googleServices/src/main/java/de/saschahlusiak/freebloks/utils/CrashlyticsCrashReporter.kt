package de.saschahlusiak.freebloks.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics
import de.saschahlusiak.freebloks.googleServices.BuildConfig

class CrashlyticsCrashReporter : CrashReporter() {

    init {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun log(priority: Int, tag: String, message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun logException(e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }

    override fun setString(key: String, value: String) {
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }
}