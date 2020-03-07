package de.saschahlusiak.freebloks.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.jetbrains.annotations.NotNull;

import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper;
import de.saschahlusiak.freebloks.utils.GooglePlayGamesHelper.GameHelperListener;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.statistics.StatisticsActivity;

/**
 * Preferences fragment to host the items to launch the {@link StatisticsActivity}
 */
public class StatisticsFragment extends PreferenceFragment implements GameHelperListener, OnPreferenceClickListener {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;
	private static final int REQUEST_SIGN_IN = 3;

	private GooglePlayGamesHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_stats);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

		mHelper = new GooglePlayGamesHelper(getActivity().getApplicationContext(), this);

        findPreference("statistics").setOnPreferenceClickListener(this);

		int avail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (avail != ConnectionResult.SUCCESS) {
			getPreferenceScreen().removePreference(findPreference("googleplus_category"));
		} else {
			findPreference("googleplus_signin").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (mHelper.isSignedIn()) {
						mHelper.startSignOut();
						onGoogleAccountSignedOut();
					} else
						mHelper.beginUserInitiatedSignIn(StatisticsFragment.this, REQUEST_SIGN_IN);
					return true;
				}
			});
			findPreference("googleplus_leaderboard").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (!mHelper.isSignedIn())
						return false;
					mHelper.startLeaderboardIntent(StatisticsFragment.this, getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD);
					return true;
				}
			});
			findPreference("googleplus_achievements").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mHelper.startAchievementsIntent(StatisticsFragment.this, REQUEST_ACHIEVEMENTS);
					return true;
				}
			});
		}
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
			case REQUEST_SIGN_IN:
				mHelper.onActivityResult(resultCode, data);
				break;

			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
    }

	@Override
	public void onGoogleAccountSignedOut() {
		if (!isVisible())
			return;
		if (findPreference("googleplus_category") == null)
			return;

		findPreference("googleplus_signin").setTitle(R.string.googleplus_signin);
		findPreference("googleplus_signin").setSummary(R.string.googleplus_signin_long);
		findPreference("googleplus_leaderboard").setEnabled(false);
		findPreference("googleplus_achievements").setEnabled(false);
	}

	@Override
	public void onGoogleAccountSignedIn(@NotNull GoogleSignInAccount account) {
    	if (!isVisible())
    		return;
		findPreference("googleplus_signin").setTitle(R.string.googleplus_signout);
		mHelper.getPlayer(player -> {
			findPreference("googleplus_signin").setSummary(getString(R.string.googleplus_signout_long, player.getDisplayName()));
			return null;
		});
		findPreference("googleplus_leaderboard").setEnabled(true);
		findPreference("googleplus_achievements").setEnabled(true);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
    	Intent intent;
    	switch (preference.getKey()) {
			case "statistics":
				intent = new Intent(getActivity(), StatisticsActivity.class);
				startActivity(intent);
				break;

			default:
				return false;
		}
		return true;
	}
}