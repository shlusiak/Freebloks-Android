package de.saschahlusiak.freebloks.preferences;

import java.util.List;

import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.app.backup.BackupManager;
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
import android.preference.PreferenceGroup;
import android.view.MenuItem;
import android.view.ViewConfiguration;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;
	boolean hasHeaders = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= 11) {
			if (getActionBar() != null)
				getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		if (savedInstanceState != null) {
			hasHeaders = savedInstanceState.getBoolean("hasHeaders");
		}

		if (getIntent().getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT) != null)
			hasHeaders = true;

		if (hasHeaders)
			return;

		addPreferencesFromResource(R.xml.preferences);
		
		ViewConfiguration viewConfig = ViewConfiguration.get(this);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || viewConfig.hasPermanentMenuKey()) {
			Preference p = findPreference("immersive_mode");
			((PreferenceGroup)findPreference("interface_category")).removePreference(p);
		}
		
		if (Build.VERSION.SDK_INT < 11) {
			((PreferenceGroup)findPreference("misc_category")).removePreference(findPreference("notifications"));
		}

		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url;
				if (BuildConfig.IS_AMAZON)
					url = Global.getMarketURLString(getPackageName());
				else
					url = "market://details?id=" + getApplication().getPackageName();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, BuildConfig.IS_AMAZON ? "Amazon App Store" : "Google Play"));

		getPreferenceScreen().removePreference(findPreference("googleplus_category"));
	}

	@Override
	public void onBuildHeaders(List<Header> target) {
		if (!onIsMultiPane())
			return;
		hasHeaders = true;
        loadHeadersFromResource(R.xml.preference_headers, target);
        
        if (Global.IS_VIP) {
	        for (Header header: target) {
	        	if (header.id == R.id.prefs_header_donate) {
	        		target.remove(header);
	        	}
	        }
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("hasHeaders", hasHeaders);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		if (Build.VERSION.SDK_INT >= 8) {
			BackupManager backupManager = new BackupManager(this);
			backupManager.dataChanged();
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		if (!hasHeaders) {
			SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
			onSharedPreferenceChanged(prefs, "player_name");
			onSharedPreferenceChanged(prefs, "theme");
			onSharedPreferenceChanged(prefs, "animations");
			prefs.registerOnSharedPreferenceChangeListener(this);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!hasHeaders)
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
		if (key.equals("theme") || key.equals("animations")) {
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

	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (InterfaceFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (DisplayFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (MiscFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (StatisticsFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (AboutFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		return false;
	}
}
