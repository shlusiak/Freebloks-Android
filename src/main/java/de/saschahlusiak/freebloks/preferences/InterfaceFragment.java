package de.saschahlusiak.freebloks.preferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.ViewConfiguration;
import de.saschahlusiak.freebloks.R;

public class InterfaceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_interface);
        
		ViewConfiguration viewConfig = ViewConfiguration.get(getActivity());
		if (viewConfig.hasPermanentMenuKey()) {
			getPreferenceScreen().removePreference(findPreference("immersive_mode"));
		}
    }
}