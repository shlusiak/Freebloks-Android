package de.saschahlusiak.freebloks.donate

import android.app.Application
import android.view.View
import android.widget.Button
import androidx.core.view.isVisible
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.App
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class)
class DonateActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(DonateActivity::class.java)

    @Test
    fun checkDisplayStateForFlavour() {
        rule.scenario.onActivity {
            if (Global.IS_GOOGLE) {
                assertTrue(it.findViewById<View>(R.id.donateThankYou).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationsGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationFreebloksVip).isVisible)
                assertFalse(it.findViewById<View>(R.id.donationLitecoin).isVisible)
                assertFalse(it.findViewById<View>(R.id.donationPaypal).isVisible)
                assertFalse(it.findViewById<View>(R.id.donateButtonGroup).isVisible)
            } else {
                assertFalse(it.findViewById<View>(R.id.donateThankYou).isVisible)
                assertFalse(it.findViewById<View>(R.id.donationsGroup).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationFreebloksVip).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationLitecoin).isVisible)
                assertTrue(it.findViewById<View>(R.id.donationPaypal).isVisible)
                assertTrue(it.findViewById<View>(R.id.donateButtonGroup).isVisible)
            }
        }
    }

    @Test
    fun skipFinishesActivity() {
        rule.scenario.onActivity {
            it.findViewById<Button>(R.id.skip).performClick()

            assertTrue(it.isFinishing)
        }
    }

    @Test
    fun nextButtonClicked() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        rule.scenario.onActivity {
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
        }
    }

    @Test
    fun onFreebloksClick() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        rule.scenario.onActivity {
            it.findViewById<View>(R.id.donationFreebloksVip).performClick()

            val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(it.freebloksVipIntent))
        }
    }

    @Test
    fun onPaypalClick() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        rule.scenario.onActivity {
            it.findViewById<View>(R.id.donationPaypal).performClick()

            val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(it.paypalIntent))
        }
    }

    @Test
    fun onLitecoinClick() {
        val app = ApplicationProvider.getApplicationContext<Application>()

        rule.scenario.onActivity {
            it.findViewById<View>(R.id.donationLitecoin).performClick()

            val startedActivityIntent = Shadows.shadowOf(app).nextStartedActivity
            assertTrue(startedActivityIntent.filterEquals(it.litecoinIntent))
        }
    }
}