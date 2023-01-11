package de.saschahlusiak.freebloks.utils

import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

val Fragment.prefs: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(requireContext())
