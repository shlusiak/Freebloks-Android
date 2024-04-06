package de.saschahlusiak.freebloks.preferences

import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.preferences.types.ListPreferenceDialogFragment
import de.saschahlusiak.freebloks.preferences.types.ThemePreference
import de.saschahlusiak.freebloks.preferences.types.ThemePreferenceDialogFragment

/**
 * Activity to host and manage the new [SettingsFragment]
 *
 * Handles fragments and fragment navigation, in case of multi-pane layout.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {

    private var hasHeaders = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.configuration.smallestScreenWidthDp >= 600 || Feature.FORCE_TWO_PANES) {
            setContentView(R.layout.settings_activity_twopane)
        } else {
            setContentView(R.layout.settings_activity)
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
        val fragment = Class.forName(pref.fragment).newInstance() as Fragment
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
                    setTargetFragment(caller, 0)
                    show(supportFragmentManager, null)
                }
                return true
            }
            is ListPreference -> {
                ListPreferenceDialogFragment().apply {
                    setKey(pref.key)
                    setTargetFragment(caller, 0)
                    show(supportFragmentManager, null)
                }
                return true
            }

            else -> return false
        }
    }
}