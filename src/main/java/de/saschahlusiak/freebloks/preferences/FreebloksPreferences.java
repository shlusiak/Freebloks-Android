package de.saschahlusiak.freebloks.preferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.donate.DonateActivity;
import de.saschahlusiak.freebloks.game.GooglePlayGamesHelper;
import de.saschahlusiak.freebloks.game.GooglePlayGamesHelper.GameHelperListener;
import de.saschahlusiak.freebloks.rules.RulesActivity;
import de.saschahlusiak.freebloks.statistics.StatisticsActivity;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, GameHelperListener {
	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;
	private static final int REQUEST_SIGN_IN = 3;

	private GooglePlayGamesHelper mHelper;
 	private boolean hasHeaders = false;

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

		mHelper = new GooglePlayGamesHelper(this, this);

		addPreferencesFromResource(R.xml.preferences);

		findPreference("rules").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(FreebloksPreferences.this, RulesActivity.class);
				startActivity(intent);
				return true;
			}
		});

		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				final String url = Global.getMarketURLString(BuildConfig.APPLICATION_ID);
				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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
		findPreference("donate").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(FreebloksPreferences.this, DonateActivity.class);
				startActivity(intent);
				return true;
			}
		});
		findPreference("statistics").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(FreebloksPreferences.this, StatisticsActivity.class);
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
						mHelper.startSignOut();
						onGoogleAccountSignedOut();
					} else
						mHelper.beginUserInitiatedSignIn(FreebloksPreferences.this, REQUEST_SIGN_IN);
					return true;
				}
			});
			findPreference("googleplus_leaderboard").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					if (!mHelper.isSignedIn())
						return false;
					mHelper.startLeaderboardIntent(FreebloksPreferences.this, getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD);
					return true;
				}
			});
			findPreference("googleplus_achievements").setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					mHelper.startAchievementsIntent(FreebloksPreferences.this, REQUEST_ACHIEVEMENTS);
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
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if (mHelper != null && request == REQUEST_SIGN_IN)
        	mHelper.onActivityResult(response, data);
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
	public void onGoogleAccountSignedOut() {
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
	public void onGoogleAccountSignedIn(@NotNull GoogleSignInAccount account) {
		if (hasHeaders)
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
