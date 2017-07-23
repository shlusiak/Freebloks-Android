package de.saschahlusiak.freebloks.stats;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.model.Stone;

public class StatisticsActivity extends BaseGameActivity {
	HighscoreDB db;
	StatisticsAdapter adapter;
	GameMode game_mode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS;

	String[] labels;
	String[] values;

	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		db = new HighscoreDB(this);
		db.open();
		
		getGameHelper().setMaxAutoSignInAttempts(0);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics_activity);

		labels = getResources().getStringArray(R.array.statistics_labels);
		values = new String[labels.length];


		adapter = new StatisticsAdapter(this, labels, values);
		((ListView) findViewById(R.id.listView)).setAdapter(adapter);

		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		findViewById(R.id.signin).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				beginUserInitiatedSignIn();
			}
		});

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		game_mode = GameMode.from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal()));
		refreshData();

		if (getActionBar() == null) {
			((Spinner)findViewById(R.id.game_mode)).setSelection(game_mode.ordinal());
			((Spinner)findViewById(R.id.game_mode)).setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
					game_mode = GameMode.from(position);
					refreshData();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {

				}
			});
		} else {
			findViewById(R.id.game_mode).setVisibility(View.GONE);
			SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.game_modes,
					android.R.layout.simple_spinner_dropdown_item);
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			getActionBar().setListNavigationCallbacks(mSpinnerAdapter, new OnNavigationListener() {
				@Override
				public boolean onNavigationItemSelected(int itemPosition, long itemId) {
					game_mode = GameMode.from(itemPosition);
					refreshData();
					return true;
				}
			});
			getActionBar().setSelectedNavigationItem(game_mode.ordinal());
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
			findViewById(R.id.signin).setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		db.close();
		db = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stats_optionsmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.signout).setVisible(isSignedIn());
		menu.findItem(R.id.achievements).setVisible(isSignedIn());
		menu.findItem(R.id.leaderboard).setVisible(isSignedIn());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.clear:
			db.clearHighscores();
			refreshData();
			return true;
		case R.id.signout:
			signOut();
			findViewById(R.id.signin).setVisibility(View.VISIBLE);
			findViewById(R.id.leaderboard).setVisibility(View.GONE);
			findViewById(R.id.achievements).setVisibility(View.GONE);
			return true;
		case R.id.achievements:
			if (isSignedIn())
				startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), REQUEST_ACHIEVEMENTS);
			return true;
		case R.id.leaderboard:
			if (isSignedIn())
				startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getApiClient(), getString(R.string.leaderboard_points_total)), REQUEST_LEADERBOARD);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void refreshData() {
		int games = db.getTotalNumberOfGames(game_mode);
		int points = db.getTotalNumberOfPoints(game_mode);
		int perfect = db.getNumberOfPerfectGames(game_mode);
		int good = db.getNumberOfGoodGames(game_mode);
		int stones_left = db.getTotalNumberOfStonesLeft(game_mode);
		int stones_used = games * Stone.STONE_COUNT_ALL_SHAPES - stones_left;
		int i;


		for (i = 0; i < values.length; i++)
			values[i] = "";

		values[0] = String.format("%d", games);
		values[8] = String.format("%d", points);

		if (games == 0) /* avoid divide by zero */ {
			games = 1;
			stones_used = 0;
		}

		good -= perfect;
		values[1] = String.format("%.1f%%", 100.0f * (float)good / (float)games);
		values[2] = String.format("%.1f%%", 100.0f * (float)perfect / (float)games);
		for (i = 0; i < 4; i++) {
			int n = db.getNumberOfPlace(game_mode, i + 1);
			values[3 + i] = String.format("%.1f%%", 100.0f * (float)n / (float)games);
		}
		if (game_mode == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			game_mode == GameMode.GAMEMODE_DUO ||
			game_mode == GameMode.GAMEMODE_JUNIOR) {
			values[5] = values[6] = null;
		}
		values[7] = String.format("%.1f%%", 100.0f * (float)stones_used / (float)games / (float)Stone.STONE_COUNT_ALL_SHAPES);

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onSignInFailed() {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
			return;

		findViewById(R.id.signin).setVisibility(View.VISIBLE);
		invalidateOptionsMenu();
	}

	@Override
	public void onSignInSucceeded() {
		findViewById(R.id.signin).setVisibility(View.GONE);
		invalidateOptionsMenu();

		if (db == null)
			return;
		
		Games.Leaderboards.submitScore(
				getApiClient(),
				getString(R.string.leaderboard_games_won),
				db.getNumberOfPlace(null, 1));

		Games.Leaderboards.submitScore(
				getApiClient(),
				getString(R.string.leaderboard_points_total),
				db.getTotalNumberOfPoints(null));
	}
}
