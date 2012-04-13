package de.saschahlusiak.freebloks.preferences;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.R.xml;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class FreebloksPreferences extends PreferenceActivity {
	Preference clear_cache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
	}
}
