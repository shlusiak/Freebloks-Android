package de.saschahlusiak.freebloks.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsCrashReporter(val isEnabled: Boolean): CrashReporter {
    init {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isEnabled)
    }

    override fun log(message: String) = FirebaseCrashlytics.getInstance().log(message)

    override fun logException(e: Exception) = FirebaseCrashlytics.getInstance().recordException(e)

    override fun setString(key: String, value: String) = FirebaseCrashlytics.getInstance().setCustomKey(key, value)
}