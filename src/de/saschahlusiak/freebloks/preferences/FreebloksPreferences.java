package de.saschahlusiak.freebloks.preferences;

import de.saschahlusiak.freebloks.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class FreebloksPreferences extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);		
	}
}
