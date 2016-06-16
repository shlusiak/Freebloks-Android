package de.saschahlusiak.freebloks.preferences;

import android.annotation.TargetApi;
import android.os.Build;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import de.saschahlusiak.freebloks.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StatisticsFragment extends PreferenceFragment {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_stats);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

		getPreferenceScreen().removePreference(findPreference("googleplus_category"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	mHelper.onActivityResult(requestCode, resultCode, data);
    	/* the onActivityResult of the activity will be called :-/ */
    }
}