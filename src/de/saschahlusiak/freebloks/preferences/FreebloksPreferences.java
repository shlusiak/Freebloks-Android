package de.saschahlusiak.freebloks.preferences;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url;
				if (Global.IS_AMAZON)
					url = Global.getMarketURLString();
				else
					url = "market://details?id=" + Global.PACKAGE_NAME;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, Global.IS_AMAZON ? "Amazon App Store" : "Google Play"));
		
		/* TODO: implement Amazon specific donation activity and enable preference */
		if (Global.IS_AMAZON)
			((PreferenceCategory)findPreference("about_category")).removePreference(findPreference("donate"));
	}
	
	@Override
	protected void onResume() {
		SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
		onSharedPreferenceChanged(prefs, "player_name");
		onSharedPreferenceChanged(prefs, "theme");
		prefs.registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("player_name")) {
			EditTextPreference pref = (EditTextPreference)findPreference(key);
			String s = pref.getText();
			if (s == null || s.equals(""))
				s = getString(R.string.prefs_player_name_default);
			pref.setSummary(s);
		}
		if (key.equals("theme")) {
			ListPreference pref = (ListPreference)findPreference(key);
			pref.setSummary(pref.getEntry());
		}
	}
}
