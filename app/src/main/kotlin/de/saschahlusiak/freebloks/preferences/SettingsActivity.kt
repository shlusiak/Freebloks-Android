package de.saschahlusiak.freebloks.preferences

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
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
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

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