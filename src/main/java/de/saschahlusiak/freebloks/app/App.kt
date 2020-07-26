package de.saschahlusiak.freebloks.app

import android.app.Application
import de.saschahlusiak.freebloks.DependencyProvider

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        DependencyProvider.initialise(this)
    }
}