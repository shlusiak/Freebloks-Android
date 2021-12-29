package de.saschahlusiak.freebloks.app

import android.app.Application
import androidx.annotation.Keep
import dagger.hilt.android.HiltAndroidApp
import de.saschahlusiak.freebloks.DependencyProvider

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DependencyProvider.initialise(this)
    }
}