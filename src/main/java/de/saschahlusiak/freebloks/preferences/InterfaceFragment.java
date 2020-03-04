package de.saschahlusiak.freebloks.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.saschahlusiak.freebloks.R;

public class InterfaceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_interface);
    }
}