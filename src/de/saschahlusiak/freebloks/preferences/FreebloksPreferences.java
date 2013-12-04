package de.saschahlusiak.freebloks.preferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.app.backup.BackupManager;
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
import android.preference.PreferenceCategory;
import android.view.MenuItem;
import android.widget.Toast;

public class FreebloksPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener, GameHelperListener {
	private static final int REQUEST_LEADERBOARD = 1;
	GameHelper mHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new GameHelper(this);
        mHelper.setup(this, GameHelper.CLIENT_GAMES, (String[])null);

		addPreferencesFromResource(R.xml.preferences);
		
		if (Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		
		findPreference("rate_review").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String url;
				if (Global.IS_AMAZON)
					url = Global.getMarketURLString(getPackageName());
				else
					url = "market://details?id=" + getApplication().getPackageName();
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
				startActivity(intent);
				return true;
			}
		});
		findPreference("rate_review").setTitle(getString(R.string.prefs_rate_review, Global.IS_AMAZON ? "Amazon App Store" : "Google Play"));
		
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
					startActivityForResult(mHelper.getGamesClient().getLeaderboardIntent(getString(R.string.leaderboard_points_total)), REQUEST_LEADERBOARD);
					return true;
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		if (Build.VERSION.SDK_INT >= 8) {
			BackupManager backupManager = new BackupManager(this);
			backupManager.dataChanged();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mHelper.onStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHelper.onStop();
	}
	
    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        mHelper.onActivityResult(request, response, data);
    }
	
	@Override
	protected void onResume() {
		SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
		onSharedPreferenceChanged(prefs, "player_name");
		onSharedPreferenceChanged(prefs, "theme");
		prefs.registerOnSharedPreferenceChangeListener(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
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
		if (key.equals("theme")) {
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
		if (findPreference("googleplus_category") == null)
			return;
		
		findPreference("googleplus_signin").setTitle(R.string.googleplus_signin);
		findPreference("googleplus_signin").setSummary(R.string.googleplus_signin_long);
		findPreference("googleplus_leaderboard").setEnabled(false);
	}

	@Override
	public void onSignInSucceeded() {
		findPreference("googleplus_signin").setTitle(R.string.googleplus_signout);
		findPreference("googleplus_signin").setSummary(getString(R.string.googleplus_signout_long, mHelper.getGamesClient().getCurrentPlayer().getDisplayName()));
		findPreference("googleplus_leaderboard").setEnabled(true);		
	}
}
