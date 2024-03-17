package de.saschahlusiak.freebloks.donate

import android.app.Application
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.utils.AnalyticsProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [21, 32])
class DonateActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun checkDisplayStateForFlavour() {
        ActivityScenario.launch(DonateActivity::class.java).onActivity {
            if (Global.IS_GOOGLE) {
                assertTrue(it.findViewById<View>(R.id.donateThankYou).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationsGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationFreebloksVip).isVisible)
                assertFalse(it.findViewById<View>(R.id.donationPaypal).isVisible)
                assertFalse(it.findViewById<View>(R.id.donateButtonGroup).isVisible)
            } else {
                assertFalse(it.findViewById<View>(R.id.donateThankYou).isVisible)
                assertFalse(it.findViewById<View>(R.id.donationsGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationFreebloksVip).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationPaypal).isVisible)
                assertTrue(it.findViewById<View>(R.id.donateButtonGroup).isVisible)
            }
        }.close()
    }

    @Test
    fun skipFinishesActivity() {
        ActivityScenario.launch(DonateActivity::class.java).onActivity {
            it.findViewById<Button>(R.id.skip).performClick()

            assertTrue(it.isFinishing)
        }.close()
    }

    @Test
    fun nextButtonClicked() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        ActivityScenario.launch(DonateActivity::class.java).onActivity {
            it.findViewById<Button>(R.id.next).performClick()

            if (Global.IS_GOOGLE) {
                // For Google this immediately launches Freebloks VIP Intent

                val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
                assertTrue(startedActivityIntent.filterEquals(it.freebloksVipIntent))
            } else {
                // For other flavours the button groups are shown

                assertFalse(it.findViewById<View>(R.id.block1).isVisible)
                assertFalse(it.findViewById<View>(R.id.donateButtonGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationsGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donateThankYou).isVisible)
            }
        }.close()
    }

    @Test
    fun onFreebloksClick() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        ActivityScenario.launch(DonateActivity::class.java).onActivity {
            it.findViewById<View>(R.id.donationFreebloksVip).performClick()

            val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(it.freebloksVipIntent))
        }.close()
    }

    @Test
    fun onPaypalClick() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        ActivityScenario.launch(DonateActivity::class.java).onActivity {
            it.findViewById<View>(R.id.donationPaypal).performClick()

            val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(it.paypalIntent))
        }.close()
    }
}