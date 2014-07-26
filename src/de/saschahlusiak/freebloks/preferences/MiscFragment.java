package de.saschahlusiak.freebloks.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import de.saschahlusiak.freebloksvip.R;

public class MiscFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_misc);
    }

    @Override
    public void onResume() {
		SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
		onSharedPreferenceChanged(prefs, "player_name");
		prefs.registerOnSharedPreferenceChangeListener(this);
    	super.onResume();
    }

    @Override
    public void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    	super.onPause();
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,	String key) {
		if (key.equals("player_name")) {
			EditTextPreference pref = (EditTextPreference)findPreference(key);
			String s = pref.getText();
			if (s == null || s.equals(""))
				s = getString(R.string.prefs_player_name_default);
			pref.setSummary(s);
		}
	}
}