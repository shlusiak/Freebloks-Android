package de.saschahlusiak.freebloks.game;

import android.database.sqlite.SQLiteException;
import com.crashlytics.android.Crashlytics;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.PlayerScore;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;
import de.saschahlusiak.freebloks.stats.StatisticsActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class GameFinishActivity extends BaseGameActivity {
	public static final int RESULT_NEW_GAME = RESULT_FIRST_USER + 1;
	public static final int RESULT_SHOW_MENU = RESULT_FIRST_USER + 2;

	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;

	TextView place;
	MessageServerStatus lastStatus;
	String clientName;
	Game game;
	boolean firstRun = false;
	PlayerScore[] data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null)
			firstRun = true;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.game_finish_activity);

		place = findViewById(R.id.place);

		game = (Game)getIntent().getSerializableExtra("game");
		lastStatus = (MessageServerStatus) getIntent().getSerializableExtra("lastStatus");
		clientName = getIntent().getStringExtra("clientName");

		if (game == null)
			throw new IllegalArgumentException("Game cannot be null");

		data = game.getPlayerScores();
		updateViews(data, game.getGameMode());

		findViewById(R.id.new_game).setOnClickListener(v -> {
			setResult(RESULT_NEW_GAME);
			finish();
		});
		findViewById(R.id.show_main_menu).setOnClickListener(v -> {
			setResult(RESULT_SHOW_MENU);
			finish();
		});
		findViewById(R.id.statistics).setOnClickListener(v -> {
			Intent intent = new Intent(GameFinishActivity.this, StatisticsActivity.class);
			startActivity(intent);
		});
		findViewById(R.id.achievements).setOnClickListener(v -> startAchievementsIntent(REQUEST_ACHIEVEMENTS));
		findViewById(R.id.leaderboard).setOnClickListener(v -> startLeaderboardIntent(getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD));
	}

	void updateViews(PlayerScore[] data, GameMode game_mode) {
		ViewGroup[] t = new ViewGroup[4];

		int i;

		t[0] = findViewById(R.id.place1);
		t[1] = findViewById(R.id.place2);
		t[2] = findViewById(R.id.place3);
		t[3] = findViewById(R.id.place4);

		if (game_mode == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			game_mode == GameMode.GAMEMODE_DUO ||
			game_mode == GameMode.GAMEMODE_JUNIOR ||
			game_mode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
			t[2].setVisibility(View.GONE);
			t[3].setVisibility(View.GONE);
		} else {
			t[2].setVisibility(View.VISIBLE);
			t[3].setVisibility(View.VISIBLE);
		}

		this.place.setText(R.string.game_finished);

		for (i = data.length - 1; i >= 0; i--) {
			String name;
			int color = Global.getPlayerColor(data[i].getPlayer1(), game_mode);
			if (clientName != null && data[i].isLocal())
				name = clientName;
			else if (lastStatus == null)
				name = getResources().getStringArray(R.array.color_names)[color];
			else
				name = lastStatus.getPlayerName(getResources(), data[i].getPlayer1(), color);

			String s;
			((TextView)t[i].findViewById(R.id.name)).setText(name);
			t[i].findViewById(R.id.name).clearAnimation();

			((TextView)t[i].findViewById(R.id.place)).setText(String.format("%d.", data[i].getPlace()));

			s = getResources().getQuantityString(R.plurals.number_of_points, data[i].getPoints(), data[i].getPoints());
			((TextView)t[i].findViewById(R.id.points)).setText(s);
			s = "";
			if (data[i].getBonus() > 0)
				s += " (+" + data[i].getBonus()+ ")";

			((TextView)t[i].findViewById(R.id.bonus_points)).setText(s);

			((TextView)t[i].findViewById(R.id.stones)).setText(
					getResources().getQuantityString(R.plurals.number_of_stones_left, data[i].getStonesLeft(), data[i].getStonesLeft()));

			t[i].findViewById(R.id.data).setBackgroundDrawable(getScoreDrawable(data[i]));

			AnimationSet set = new AnimationSet(false);
			Animation a = new AlphaAnimation(0.0f, 1.0f);
			a.setStartOffset(i * 100);
			a.setDuration(600);
			a.setFillBefore(true);
			set.addAnimation(a);
			a = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_SELF,
					1,
					TranslateAnimation.RELATIVE_TO_SELF,
					0,
					TranslateAnimation.RELATIVE_TO_SELF,
					0,
					TranslateAnimation.RELATIVE_TO_SELF,
					0);
			a.setStartOffset(200 + i * 100);
			a.setDuration(600);
			a.setFillBefore(true);
			set.addAnimation(a);

			if (data[i].isLocal()) {
				a = new TranslateAnimation(
						TranslateAnimation.RELATIVE_TO_SELF,
						0,
						TranslateAnimation.RELATIVE_TO_SELF,
						0.4f,
						TranslateAnimation.RELATIVE_TO_SELF,
						0,
						TranslateAnimation.RELATIVE_TO_SELF,
						0);
				a.setDuration(300);
				a.setInterpolator(new DecelerateInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);

				((TextView)t[i].findViewById(R.id.name)).setTextColor(Color.WHITE);
				((TextView)t[i].findViewById(R.id.place)).setTextColor(Color.WHITE);
				((TextView)t[i].findViewById(R.id.name)).setTypeface(Typeface.DEFAULT_BOLD);
				((TextView)t[i].findViewById(R.id.stones)).setTextColor(Color.WHITE);

				t[i].findViewById(R.id.name).startAnimation(a);

				a = new AlphaAnimation(0.5f, 1.0f);
				a.setDuration(750);
				a.setInterpolator(new LinearInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);

				set.addAnimation(a);

				this.place.setText(getResources().getStringArray(R.array.places)[data[i].getPlace() - 1]);
			}
			t[i].findViewById(R.id.data).startAnimation(set);
		}
	}

	Drawable getScoreDrawable(PlayerScore data) {
		int color = Global.getPlayerColor(data.getPlayer1(), game.getGameMode());
		LayerDrawable l;

		if (data.getPlayer2() >= 0)
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_2).mutate();
		else
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_1).mutate();

		((GradientDrawable)l.findDrawableByLayerId(R.id.color1)).setColor(getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[color]));
		if (data.getPlayer2() >= 0) {
			color = Global.getPlayerColor(data.getPlayer2(), game.getGameMode());
			((GradientDrawable)l.findDrawableByLayerId(R.id.color2)).setColor(getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[color]));
		}

		return l;
	}

	@Override
	public void onSignInFailed() {
		findViewById(R.id.achievements).setVisibility(View.GONE);
		findViewById(R.id.leaderboard).setVisibility(View.GONE);
	}

	@Override
	public void onSignInSucceeded() {
		findViewById(R.id.achievements).setVisibility(View.VISIBLE);
		findViewById(R.id.leaderboard).setVisibility(View.VISIBLE);

		if (!firstRun)
			return;

		for (int i = 0; i < data.length; i++) if (data[i].isLocal()) {
				if (game.getGameMode() == GameMode.GAMEMODE_4_COLORS_4_PLAYERS
						&& data[i].getPlace() == 1)
					unlock(getString(R.string.achievement_blokus_classic));

				if (game.getGameMode() == GameMode.GAMEMODE_4_COLORS_4_PLAYERS
						&& data[i].isPerfect())
					unlock(getString(R.string.achievement_perfect));

				if (game.getGameMode() == GameMode.GAMEMODE_DUO
						&& data[i].getPlace() == 1)
					unlock(getString(R.string.achievement_blokus_duo));

				increment(getString(R.string.achievement_1000_points), data[i].getPoints());

				if (data[i].getPlace() == 1)
					increment(getString(R.string.achievement_winner), 1);

				if (game.getGameMode() == GameMode.GAMEMODE_4_COLORS_4_PLAYERS
						&& data[i].getPlace()== 4)
					increment(getString(R.string.achievement_loser), 1);

				if (lastStatus != null && lastStatus.getClients() >= 4 && data[i].getPlace() == 1)
					unlock(getString(R.string.achievement_multiplayer));
			}

		increment(getString(R.string.achievement_addicted), 1);

		HighscoreDB db = new HighscoreDB(this);
		try {
			db.open();
		}
		catch (SQLiteException e) {
			Crashlytics.logException(e);
			return;
		}

		int n = 0;
		for (int i = 0; i < 4; i++)
			if (db.getNumberOfPlace(GameMode.GAMEMODE_4_COLORS_4_PLAYERS, 1, i) > 0)
				n++;
		if (db.getNumberOfPlace(GameMode.GAMEMODE_DUO, 1, 0) > 0)
			n++;
		if (db.getNumberOfPlace(GameMode.GAMEMODE_DUO, 1, 2) > 0)
			n++;
		if (n == 6)
			unlock(getString(R.string.achievement_all_colors));

		submitScore(
			getString(R.string.leaderboard_games_won),
			db.getNumberOfPlace(null, 1));

		submitScore(
			getString(R.string.leaderboard_points_total),
			db.getTotalNumberOfPoints(null));

		db.close();
	}
}
