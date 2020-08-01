package de.saschahlusiak.freebloks.app

import android.app.Application
import androidx.annotation.Keep
import de.saschahlusiak.freebloks.DependencyProvider

@Keep
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DependencyProvider.initialise(this)
    }
}