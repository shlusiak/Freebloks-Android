package de.saschahlusiak.freebloks.game

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel

class FreebloksActivityViewModel(app: Application) : AndroidViewModel(app) {
    private val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    var vibrateOnMove: Boolean = false

    init {
        reloadPreferences()
    }

    fun reloadPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(getApplication())

        vibrateOnMove = prefs.getBoolean("vibrate", true)
    }

    fun vibrate(milliseconds: Long) {
        if (vibrateOnMove)
            vibrator?.vibrate(milliseconds)
    }
}