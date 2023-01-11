package de.saschahlusiak.freebloks.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

val Context.prefs: SharedPreferences get() = PreferenceManager.getDefaultSharedPreferences(this)

fun Float.toPixel(context: Context) = this * context.resources.displayMetrics.density