package de.saschahlusiak.freebloks.preferences

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.support.SupportFragment

/**
 * The headers for [SettingsActivity] if in multi pane mode
 */
class HeadersFragment : PreferenceFragmentCompat() {
    private lateinit var headers: List<TwoStatePreference?>

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_headers, rootKey)

        headers = listOf(
            findPreference(SettingsFragment.SCREEN_INTERFACE),
            findPreference(SettingsFragment.SCREEN_DISPLAY),
            findPreference(SettingsFragment.SCREEN_MISC),
            findPreference(SettingsFragment.SCREEN_STATISTICS),
            findPreference(SettingsFragment.SCREEN_ABOUT)
        )

        headers.forEach { pref ->
            pref?.setOnPreferenceClickListener { onPreferenceClick(pref) }
        }

        // donate is not a TwoStatePreference
        findPreference<Preference>(SettingsFragment.SCREEN_SUPPORT)?.apply {
            if (Global.IS_VIP) {
                isVisible = false
            } else {
                setOnPreferenceClickListener {
                    SupportFragment().show(parentFragmentManager, null)
                    true
                }
            }
        }

        arguments?.getString(SettingsFragment.KEY_SCREEN)?.let {
            setChecked(it)
        }
    }

    private fun onPreferenceClick(pref: TwoStatePreference): Boolean {
        setChecked(pref.key)

        // fall through to default handler
        return false
    }

    private fun setChecked(key: String) {
        // uncheck all others
        headers.forEach { it?.isChecked = false }
        findPreference<TwoStatePreference>(key)?.isChecked = true
    }
}