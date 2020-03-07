package de.saschahlusiak.freebloks.utils

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics

val Context.analytics get() = FirebaseAnalytics.getInstance(this)
val Context.prefs get() = PreferenceManager.getDefaultSharedPreferences(this)
