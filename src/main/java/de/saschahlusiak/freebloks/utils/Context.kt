package de.saschahlusiak.freebloks.utils

import android.content.Context
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.DependencyProvider

val Context.prefs get() = PreferenceManager.getDefaultSharedPreferences(this)
