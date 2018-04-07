package de.saschahlusiak.freebloks.preferences;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;
import com.google.android.gms.games.Games;

import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.view.MenuItem;
import android.view.ViewConfiguration;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, GameHelperListener {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;
	GameHelper mHelper;
	boolean hasHeaders = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState != null) {
			hasHeaders = savedInstanceState.getBoolean("hasHeaders");
		}

		if (getIntent().getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT) != null)
			hasHeaders = true;

		if (hasHeaders)
			return;

		mHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
		mHelper.setMaxAutoSignInAttempts(0);
        mHelper.setup(this);

		addPreferencesFromResource(R.xml.preferences);
		
		ViewConfiguration viewConfig = ViewConfiguration.get(this);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || viewConfig.hasPermanentMenuKey()) {
			Preference p = findPreference("immersive_mode");
			((PreferenceGroup)findPreference("interface_category")).removePreference(p);
		}
		
		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url;
				if (BuildConfig.IS_AMAZON)
					url = Global.getMarketURLString(getPackageName());
				else
					url = "market://details?id=" + getApplication().getPackageName();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		findPreference("about").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(FreebloksPreferences.this, AboutActivity.class);
				startActivity(intent);
				return true;
			}
		});
		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, BuildConfig.IS_AMAZON ? "Amazon App Store" : "Google Play"));

		int avail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
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
	public void onBuildHeaders(List<Header> target) {
		if (!onIsMultiPane())
			return;
		hasHeaders = true;
        loadHeadersFromResource(R.xml.preference_headers, target);
        
        if (Global.IS_VIP) {
	        for (Header header: target) {
	        	if (header.id == R.id.prefs_header_donate) {
	        		target.remove(header);
	        	}
	        }
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("hasHeaders", hasHeaders);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mHelper != null)
			mHelper.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mHelper != null)
			mHelper.onStop();
	}

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (mHelper != null)
        	mHelper.onActivityResult(request, response, data);
    }

	@Override
	protected void onResume() {
		if (!hasHeaders) {
			SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
			onSharedPreferenceChanged(prefs, "player_name");
			onSharedPreferenceChanged(prefs, "theme");
			onSharedPreferenceChanged(prefs, "animations");
			prefs.registerOnSharedPreferenceChangeListener(this);
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!hasHeaders)
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("player_name")) {
			EditTextPreference pref = (EditTextPreference)findPreference(key);
			String s = pref.getText();
			if (s == null || s.equals(""))
				s = getString(R.string.prefs_player_name_default);
			pref.setSummary(s);
		}
		if (key.equals("theme") || key.equals("animations")) {
			ListPreference pref = (ListPreference)findPreference(key);
			pref.setSummary(pref.getEntry());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSignInFailed() {
		if (hasHeaders)
			return;
		if (findPreference("googleplus_category") == null)
			return;

		findPreference("googleplus_signin").setTitle(R.string.googleplus_signin);
		findPreference("googleplus_signin").setSummary(R.string.googleplus_signin_long);
		findPreference("googleplus_leaderboard").setEnabled(false);
		findPreference("googleplus_achievements").setEnabled(false);
	}

	@Override
	public void onSignInSucceeded() {
		if (hasHeaders)
			return;
		findPreference("googleplus_signin").setTitle(R.string.googleplus_signout);
		findPreference("googleplus_signin").setSummary(getString(R.string.googleplus_signout_long, Games.Players.getCurrentPlayer(mHelper.getApiClient()).getDisplayName()));
		findPreference("googleplus_leaderboard").setEnabled(true);
		findPreference("googleplus_achievements").setEnabled(true);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (InterfaceFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (DisplayFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (MiscFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (StatisticsFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		if (AboutFragment.class.getCanonicalName().equals(fragmentName))
			return true;
		return false;
	}
}
