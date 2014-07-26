package de.saschahlusiak.freebloks.preferences;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.saschahlusiak.freebloksvip.R;
import android.view.ViewConfiguration;

public class InterfaceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_interface);
        
		ViewConfiguration viewConfig = ViewConfiguration.get(getActivity());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || viewConfig.hasPermanentMenuKey()) {
			getPreferenceScreen().removePreference(findPreference("immersive_mode"));
		}
    }
}
