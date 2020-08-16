package de.saschahlusiak.freebloks.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.saschahlusiak.freebloks.*
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.preferences.SettingsFragment.Companion.KEY_SCREEN
import de.saschahlusiak.freebloks.preferences.SettingsFragment.Companion.KEY_SHOW_CATEGORY
import de.saschahlusiak.freebloks.preferences.SettingsFragment.Companion.SCREEN_INTERFACE
import de.saschahlusiak.freebloks.rules.RulesActivity
import de.saschahlusiak.freebloks.statistics.StatisticsActivity

/**
 * Root preferences screen, hosted in [SettingsActivity].
 *
 * Supports all possible preferences, however not all may be shown the same time (e.g. multi-pane view).
 *
 * Arguments:
 * - [KEY_SCREEN] which screen to show (e.g. [SCREEN_INTERFACE]. If unset, show all.
 * - [KEY_SHOW_CATEGORY], true or false whether to add the category header
 */
class SettingsFragment: PreferenceFragmentCompat() {
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(SettingsActivityViewModel::class.java) }

    private val REQUEST_GOOGLE_SIGN_IN = 1000
    private val REQUEST_GOOGLE_LEADERBOARD = 1001
    private val REQUEST_GOOGLE_ACHIEVEMENTS = 1002

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val ps = preferenceManager.createPreferenceScreen(activity)
        preferenceScreen = ps

        val screen = arguments?.getString(KEY_SCREEN, null)
        val showCategory = arguments?.getBoolean(KEY_SHOW_CATEGORY, true) ?: true

        if (screen == null || screen == SCREEN_INTERFACE) {
            addCategory(R.xml.preferences_interface, R.string.prefs_interface, showCategory)
            configureInterfacePreferences()
        }

        if (screen == null || screen == SCREEN_DISPLAY) {
            addCategory(R.xml.preferences_display, R.string.prefs_display, showCategory)
            configureDisplayPreferences()
        }

        if (screen == null || screen == SCREEN_MISC) {
            addCategory(R.xml.preferences_misc, R.string.prefs_misc, showCategory)
            configureMiscPreferences()
        }

        if (screen == null || screen == SCREEN_STATISTICS) {
            addCategory(R.xml.preferences_stats, R.string.prefs_statistics, showCategory)
            if (viewModel.googleHelper.isAvailable) {
                addCategory(R.xml.preferences_google_play, R.string.google_play_games, true)
            }
            configureStatisticsPreferences()
        }

        if (screen == null || screen == SCREEN_ABOUT) {
            addCategory(R.xml.preferences_about, R.string.about, showCategory)
            configureAboutPreferences()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isSignedIn.observe(viewLifecycleOwner, Observer { signedIn ->
            findPreference<Preference>("googleplus_signin")?.setTitle(if (signedIn) R.string.google_play_games_signout else R.string.google_play_games_signin)
            findPreference<Preference>("googleplus_leaderboard")?.isEnabled = signedIn
            findPreference<Preference>("googleplus_achievements")?.isEnabled = signedIn
        })

        viewModel.playerName.observe(viewLifecycleOwner, Observer { player ->
            findPreference<Preference>("googleplus_signin")?.apply {
                summary = if (player != null) {
                    getString(R.string.google_play_games_signout_long, player)
                } else {
                    getString(R.string.google_play_games_signin_long)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_GOOGLE_SIGN_IN -> viewModel.googleHelper.onActivityResult(resultCode, data) { error ->
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setMessage(error ?: getString(R.string.google_play_games_signin_failed))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss()}
                    show()
                }
            }
        }
    }

    /**
     * Add an optional category (category) and then the content of the given XML
     */
    private fun addCategory(@XmlRes content: Int, @StringRes category: Int? = null, showCategory: Boolean = true) {
        if (category != null && showCategory) {
            val p = PreferenceCategory(activity).apply {
                setTitle(category)
                isIconSpaceReserved = false
            }
            preferenceScreen.addPreference(p)
        }

        addPreferencesFromResource(content)
    }

    private fun configureInterfacePreferences() {
        // nothing to configure
    }

    private fun configureDisplayPreferences() {
        // nothing to configure
    }

    private fun configureMiscPreferences() {
        // nothing to configure
    }

    private fun configureStatisticsPreferences() {
        val helper = viewModel.googleHelper

        findPreference<Preference>("statistics")?.setOnPreferenceClickListener {
            analytics.logEvent("settings_statistics_click", null)

            val intent = Intent(activity, StatisticsActivity::class.java)
            startActivity(intent)
            true
        }

        findPreference<Preference>("googleplus_signin")?.setOnPreferenceClickListener {
            if (helper.isSignedIn) {
                helper.startSignOut()
            } else
                helper.beginUserInitiatedSignIn(this@SettingsFragment, REQUEST_GOOGLE_SIGN_IN)
            true
        }

        findPreference<Preference>("googleplus_leaderboard")?.setOnPreferenceClickListener {
            if (!helper.isSignedIn) return@setOnPreferenceClickListener false
            analytics.logEvent("settings_leaderboard_click", null)
            helper.startLeaderboardIntent(this@SettingsFragment, getString(R.string.leaderboard_points_total), REQUEST_GOOGLE_LEADERBOARD)
            true
        }

        findPreference<Preference>("googleplus_achievements")?.setOnPreferenceClickListener {
            analytics.logEvent("settings_achievements_click", null)
            helper.startAchievementsIntent(this@SettingsFragment, REQUEST_GOOGLE_ACHIEVEMENTS)
            true
        }
    }

    private fun configureAboutPreferences() {
        findPreference<Preference>("rules")?.setOnPreferenceClickListener {
            analytics.logEvent("settings_rules_click", null)
            val intent = Intent(activity, RulesActivity::class.java)
            startActivity(intent)
            true
        }

        findPreference<Preference>("rate_review")?.apply {
            title = getString(R.string.prefs_rate_review, BuildConfig.APP_STORE_NAME)
            setOnPreferenceClickListener {
                analytics.logEvent("settings_show_store_click")

                val uri = Uri.parse(Global.getMarketURLString(BuildConfig.APPLICATION_ID))
                startActivity(Intent("android.intent.action.VIEW", uri))

                true
            }
        }

        findPreference<Preference>("donate")?.setOnPreferenceClickListener {
            analytics.logEvent("settings_donate_click")

            val intent = Intent(requireContext(), DonateActivity::class.java)
            startActivity(intent)
            true
        }

        findPreference<Preference>("about")?.setOnPreferenceClickListener {
            analytics.logEvent("settings_about_click")

            AboutFragment().show(parentFragmentManager, null)
            true
        }
    }

    companion object {
        const val KEY_SCREEN = "EXTRA_SCREEN"
        const val KEY_SHOW_CATEGORY = "EXTRA_SHOW_CATEGORY"

        const val SCREEN_INTERFACE = "INTERFACE"
        const val SCREEN_DISPLAY = "DISPLAY"
        const val SCREEN_MISC = "MISC"
        const val SCREEN_STATISTICS = "STATISTICS"
        const val SCREEN_ABOUT = "ABOUT"
        const val SCREEN_DONATE = "DONATE"
    }
}