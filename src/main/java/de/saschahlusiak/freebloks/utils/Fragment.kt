package de.saschahlusiak.freebloks.utils

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics

val Fragment.analytics get() = FirebaseAnalytics.getInstance(requireContext())
val Fragment.prefs get() = PreferenceManager.getDefaultSharedPreferences(context)
