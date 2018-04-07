package de.saschahlusiak.freebloks.preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;

public class AboutFragment extends PreferenceFragment implements OnPreferenceClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_about);

		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, BuildConfig.IS_AMAZON ? "Amazon App Store" : "Google Play"));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
		findPreference("rate_review").setOnPreferenceClickListener(this);
		findPreference("about").setOnPreferenceClickListener(this);
    	super.onActivityCreated(savedInstanceState);
    }

	@Override
	public boolean onPreferenceClick(Preference preference) {
    	final Intent intent;

    	switch (preference.getKey()) {
			case "rate_review":
				String url;
				if (BuildConfig.IS_AMAZON)
					url = Global.getMarketURLString(getActivity().getPackageName());
				else
					url = "market://details?id=" + getActivity().getApplication().getPackageName();
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				break;

			case "about":
				intent = new Intent(getActivity(), AboutActivity.class);
				startActivity(intent);
				break;
		}

		return true;
	}
}