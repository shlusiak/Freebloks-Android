package de.saschahlusiak.freebloks.preferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import de.saschahlusiak.freebloksvip.R;

public class StatisticsFragment extends PreferenceFragment implements GameHelperListener {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;

	GameHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_stats);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);

		mHelper = new GameHelper(getActivity(), GameHelper.CLIENT_GAMES);
		mHelper.setMaxAutoSignInAttempts(0);
        mHelper.setup(this);

        /* XXX: this is a hack, because the onActivityResult of the activity will be called instead of the fragment */
        /* make sure that the activity knows about the helper, which is created in the context of the fragment. bah. */
        ((FreebloksPreferences)getActivity()).mHelper = mHelper;

		int avail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (avail != ConnectionResult.SUCCESS) {
			getPreferenceScreen().removePreference(findPreference("googleplus_category"));
		} else {
			findPreference("googleplus_signin").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (mHelper.isSignedIn()) {
						mHelper.signOut();
						onSignInFailed();
					} else
						mHelper.beginUserInitiatedSignIn();
					return true;
				}
			});
			findPreference("googleplus_leaderboard").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (!mHelper.isSignedIn())
						return false;
	//				startActivityForResult(mHelper.getGamesClient().getAllLeaderboardsIntent(), REQUEST_LEADERBOARD);
					startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mHelper.getApiClient(), getString(R.string.leaderboard_points_total)), REQUEST_LEADERBOARD);
					return true;
				}
			});
			findPreference("googleplus_achievements").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivityForResult(Games.Achievements.getAchievementsIntent(mHelper.getApiClient()), REQUEST_ACHIEVEMENTS);
					return true;
				}
			});
		}
    }

    @Override
    public void onStart() {
    	super.onStart();
    	mHelper.onStart(getActivity());
    }

    @Override
    public void onStop() {
    	super.onStop();
    	mHelper.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
//    	mHelper.onActivityResult(requestCode, resultCode, data);
    	/* the onActivityResult of the activity will be called :-/ */
    }

	@Override
	public void onSignInFailed() {
		if (findPreference("googleplus_category") == null)
			return;

		findPreference("googleplus_signin").setTitle(R.string.googleplus_signin);
		findPreference("googleplus_signin").setSummary(R.string.googleplus_signin_long);
		findPreference("googleplus_leaderboard").setEnabled(false);
		findPreference("googleplus_achievements").setEnabled(false);
	}

	@Override
	public void onSignInSucceeded() {
		findPreference("googleplus_signin").setTitle(R.string.googleplus_signout);
		findPreference("googleplus_signin").setSummary(getString(R.string.googleplus_signout_long, Games.Players.getCurrentPlayer(mHelper.getApiClient()).getDisplayName()));
		findPreference("googleplus_leaderboard").setEnabled(true);
		findPreference("googleplus_achievements").setEnabled(true);
	}
}