package de.saschahlusiak.freebloks.game

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.App
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [32])
class FreebloksActivityTest {
    @Test
    fun launchSettingsFromMenu() {
        val scenario = ActivityScenario.launch(FreebloksActivity::class.java)
        val app = ApplicationProvider.getApplicationContext<Application>()

        scenario.onActivity {
            it.showMainMenu()
            // FIXME: assert on something
        }
    }
}