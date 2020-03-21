package de.saschahlusiak.freebloks.utils

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.DependencyProvider

val Fragment.prefs get() = PreferenceManager.getDefaultSharedPreferences(context)
