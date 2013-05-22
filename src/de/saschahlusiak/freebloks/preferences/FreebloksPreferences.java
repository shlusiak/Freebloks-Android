package de.saschahlusiak.freebloks.preferences;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class FreebloksPreferences extends PreferenceActivity {
	
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
	}
}
