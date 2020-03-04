package de.saschahlusiak.freebloks.statistics;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.jetbrains.annotations.NotNull;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GooglePlayGamesHelper;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.model.Shape;

public class StatisticsActivity extends FragmentActivity implements GooglePlayGamesHelper.GameHelperListener {
	private HighscoreDB db;
	private StatisticsAdapter adapter;
	private GameMode game_mode = GameMode.GAMEMODE_4_COLORS_4_PLAYERS;

	private String[] labels;
	private String[] values;

	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;
	private static final int REQUEST_SIGN_IN = 3;

	private GooglePlayGamesHelper gameHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		gameHelper = new GooglePlayGamesHelper(this, this);

		db = new HighscoreDB(this);
		db.open();
		
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
				gameHelper.beginUserInitiatedSignIn(StatisticsActivity.this, REQUEST_SIGN_IN);
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
		boolean isSignedIn = gameHelper.isSignedIn();
		menu.findItem(R.id.signout).setVisible(isSignedIn);
		menu.findItem(R.id.achievements).setVisible(isSignedIn);
		menu.findItem(R.id.leaderboard).setVisible(isSignedIn);
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
			gameHelper.startSignOut();
			findViewById(R.id.signin).setVisibility(View.VISIBLE);
			findViewById(R.id.leaderboard).setVisibility(View.GONE);
			findViewById(R.id.achievements).setVisibility(View.GONE);
			return true;
		case R.id.achievements:
			if (gameHelper.isSignedIn())
				gameHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS);
			return true;
		case R.id.leaderboard:
			if (gameHelper.isSignedIn())
				gameHelper.startLeaderboardIntent(this, getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		switch (requestCode) {
			case REQUEST_SIGN_IN:
				gameHelper.onActivityResult(resultCode, data);
				break;

			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void refreshData() {
		int games = db.getTotalNumberOfGames(game_mode);
		int points = db.getTotalNumberOfPoints(game_mode);
		int perfect = db.getNumberOfPerfectGames(game_mode);
		int good = db.getNumberOfGoodGames(game_mode);
		int stones_left = db.getTotalNumberOfStonesLeft(game_mode);
		int stones_used = games * Shape.COUNT - stones_left;
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
		values[7] = String.format("%.1f%%", 100.0f * (float)stones_used / (float)games / (float) Shape.COUNT);

		adapter.notifyDataSetChanged();
	}

	@Override
	public void onGoogleAccountSignedOut() {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS)
			return;

		findViewById(R.id.signin).setVisibility(View.VISIBLE);
		invalidateOptionsMenu();
	}

	@Override
	public void onGoogleAccountSignedIn(@NotNull GoogleSignInAccount account) {
		findViewById(R.id.signin).setVisibility(View.GONE);
		invalidateOptionsMenu();

		if (db == null)
			return;

		gameHelper.submitScore(
			getString(R.string.leaderboard_games_won),
			db.getNumberOfPlace(null, 1));

		gameHelper.submitScore(
			getString(R.string.leaderboard_points_total),
			db.getTotalNumberOfPoints(null));
	}
}
