package de.saschahlusiak.freebloks.preferences

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.about.AboutFragment
import de.saschahlusiak.freebloks.app.theme.AppTheme
import de.saschahlusiak.freebloks.preferences.types.ListPreferenceDialogFragment
import de.saschahlusiak.freebloks.preferences.types.ThemePreference
import de.saschahlusiak.freebloks.preferences.types.ThemePreferenceDialogFragment
import de.saschahlusiak.freebloks.statistics.StatisticsActivity
import de.saschahlusiak.freebloks.support.SupportFragment

/**
 * Activity to host and manage the new [SettingsFragment]
 *
 * Handles fragments and fragment navigation, in case of multi-pane layout.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    private val viewModel: SettingsActivityViewModel by viewModels()

    private val REQUEST_GOOGLE_SIGN_IN = 1000
    private val REQUEST_GOOGLE_LEADERBOARD = 1001
    private val REQUEST_GOOGLE_ACHIEVEMENTS = 1002

    private var hasHeaders = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        if (Feature.COMPOSE_SETTINGS) {
            setContent {
                AppTheme {
                    Content()
                }
            }
            return
        }

        if (resources.configuration.smallestScreenWidthDp >= 600 || Feature.FORCE_TWO_PANES) {
            setContentView(R.layout.settings_activity_twopane)
        } else {
            setContentView(R.layout.settings_activity)
        }

        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val toolBarHeight = TypedValue()
            view.context.theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, toolBarHeight, true)

            view.updateLayoutParams {
                height = systemBarInsets.top + toolBarHeight.getDimension(view.resources.displayMetrics).toInt()
            }
            view.setPadding(0, systemBarInsets.top, 0, 0)

            insets
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        if (savedInstanceState == null) {
            hasHeaders = (findViewById<FrameLayout>(R.id.headers) != null)
            val f = SettingsFragment()

            if (hasHeaders) {
                val headers = HeadersFragment().apply {
                    arguments = Bundle().apply {
                        putString(SettingsFragment.KEY_SCREEN, SettingsFragment.SCREEN_INTERFACE)
                    }
                }
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.headers, headers)
                    .commit()

                f.arguments = Bundle().apply {
                    putString(SettingsFragment.KEY_SCREEN, SettingsFragment.SCREEN_INTERFACE)
                }
            } else {
                // single screen
                f.arguments = Bundle().apply {
                    putBoolean(SettingsFragment.KEY_SHOW_CATEGORY, true)
                }
            }

            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, f)
                .commit()
        }
    }

    @Composable
    private fun Content() {
        SettingsScreen(
            viewModel = viewModel,
            onBack = ::finish,
            onSignIn = ::onSignIn,
            onAchievements = ::onAchievements,
            onLeaderboard = ::onLeaderboard,
            onAbout = ::onAbout,
            onRate = ::onRate,
            onSupport = ::onSupport,
            onStatistics = ::onStatistics,
            onTheme = ::onTheme,
            onBoardTheme = ::onBoardTheme
        )
    }

    private fun onSignIn() {
        viewModel.googleHelper.beginUserInitiatedSignIn(
            activity = this,
            requestCode = REQUEST_GOOGLE_SIGN_IN
        )
    }

    private fun onAchievements() {
        viewModel.googleHelper.startAchievementsIntent(
            activity = this,
            requestCode = REQUEST_GOOGLE_ACHIEVEMENTS
        )
    }

    private fun onLeaderboard() {
        viewModel.googleHelper.startLeaderboardIntent(
            this,
            getString(R.string.leaderboard_points_total),
            REQUEST_GOOGLE_LEADERBOARD
        )
    }

    private fun onAbout() {
        AboutFragment().show(supportFragmentManager, null)
    }

    private fun onSupport() {
        SupportFragment().show(supportFragmentManager, null)
    }

    private fun onRate() {
        val uri = Global.getMarketURLString(BuildConfig.APPLICATION_ID).toUri()
        startActivity(Intent("android.intent.action.VIEW", uri))
    }

    private fun onStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun onTheme() {
        ThemePreferenceDialogFragment().apply {
            setKey("theme")
        }.show(supportFragmentManager, null)
    }

    private fun onBoardTheme() {
        ThemePreferenceDialogFragment().apply {
            setKey("board_theme")
        }.show(supportFragmentManager, null)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_SIGN_IN -> viewModel.googleHelper.onActivityResult(resultCode, data) { error ->
                MaterialAlertDialogBuilder(this).apply {
                    setMessage(error ?: getString(R.string.google_play_games_signin_failed))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                    show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = Class.forName(requireNotNull(pref.fragment))
            .getDeclaredConstructor()
            .newInstance() as Fragment

        fragment.arguments = pref.extras

        return when (caller) {
            is HeadersFragment -> {
                // just in case we are a level deep, pop the last transaction
                supportFragmentManager
                    .popBackStack()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit()

                title = pref.title

                true
            }

            is SettingsFragment -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()

                true
            }

            else -> false
        }
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        when (pref) {
            is ThemePreference -> {
                ThemePreferenceDialogFragment().apply {
                    setKey(pref.key)
                    @Suppress("DEPRECATION")
                    setTargetFragment(caller, 0)
                    show(supportFragmentManager, null)
                }
                return true
            }

            is ListPreference -> {
                ListPreferenceDialogFragment().apply {
                    setKey(pref.key)
                    @Suppress("DEPRECATION")
                    setTargetFragment(caller, 0)
                    show(supportFragmentManager, null)
                }
                return true
            }

            else -> return false
        }
    }
}