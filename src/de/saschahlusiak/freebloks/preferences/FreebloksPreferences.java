package de.saschahlusiak.freebloks.preferences;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.view.MenuItem;
import android.widget.Toast;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url;
				if (Global.IS_AMAZON)
					url = Global.getMarketURLString(getPackageName());
				else
					url = "market://details?id=" + getApplication().getPackageName();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, Global.IS_AMAZON ? "Amazon App Store" : "Google Play"));
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
