package de.saschahlusiak.freebloks.rules

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Button
import androidx.test.core.app.ActivityScenario
import de.saschahlusiak.freebloks.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import de.saschahlusiak.freebloks.app.App

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [21, 32])
class RulesActivityTest {

    @Test
    fun showVideoClick() {
        val scenario = ActivityScenario.launch(RulesActivity::class.java)
        val app = ApplicationProvider.getApplicationContext<Application>()

        scenario.onActivity {
            it.findViewById<Button>(R.id.youtube).callOnClick()

            val expectedIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=pc8nmWpcQWs")
            )

            val startedActivityIntent = shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(expectedIntent))
        }

        scenario.close()
    }
}