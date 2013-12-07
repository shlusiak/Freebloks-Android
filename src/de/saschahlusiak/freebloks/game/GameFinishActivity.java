package de.saschahlusiak.freebloks.game;

import com.google.example.games.basegameutils.BaseGameActivity;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloksvip.R;
import de.saschahlusiak.freebloks.controller.PlayerData;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
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
import android.view.View.OnClickListener;
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
	NET_SERVER_STATUS lastStatus;
	String clientName;
	Spielleiter spiel;
	boolean firstRun = false;
	PlayerData[] data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState == null)
			firstRun = true;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		

		setContentView(R.layout.game_finish_activity);
		
	    
		place = (TextView) findViewById(R.id.place);
		
		spiel = (Spielleiter)getIntent().getSerializableExtra("game");
		lastStatus = (NET_SERVER_STATUS)getIntent().getSerializableExtra("lastStatus");
		clientName = getIntent().getStringExtra("clientName");

		data = spiel.getResultData();
		updateViews(data, spiel.m_gamemode);

		findViewById(R.id.new_game).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				setResult(RESULT_NEW_GAME);
				finish();
			}
		});
		findViewById(R.id.show_main_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_SHOW_MENU);
				finish();
			}
		});
		findViewById(R.id.statistics).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(GameFinishActivity.this, StatisticsActivity.class);
				startActivity(intent);
			}
		});
		findViewById(R.id.achievements).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(getGamesClient().getAchievementsIntent(), REQUEST_ACHIEVEMENTS);
			}
		});
		findViewById(R.id.leaderboard).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(getGamesClient().getLeaderboardIntent(getString(R.string.leaderboard_points_total)), REQUEST_LEADERBOARD);
			}
		});
	}
		
	void updateViews(PlayerData[] data, int game_mode) {
		ViewGroup t[] = new ViewGroup[4];

		int i = 0;
		
		t[0] = (ViewGroup) findViewById(R.id.place1);
		t[1] = (ViewGroup) findViewById(R.id.place2);
		t[2] = (ViewGroup) findViewById(R.id.place3);
		t[3] = (ViewGroup) findViewById(R.id.place4);
		
		/* TODO: combine yellow/green, blue/red on 4_COLORS_2_PLAYERS */
		if (game_mode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
			game_mode == Spielleiter.GAMEMODE_DUO ||
			game_mode == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
			t[2].setVisibility(View.GONE);
			t[3].setVisibility(View.GONE);
		} else {
			t[2].setVisibility(View.VISIBLE);
			t[3].setVisibility(View.VISIBLE);
		}

		this.place.setText(R.string.game_finished);

		for (i = data.length - 1; i >= 0; i--) {
			String name;
			int color = Global.getPlayerColor(data[i].player1, game_mode);
			if (clientName != null && data[i].is_local)
				name = clientName; 
			else if (lastStatus == null)
				name = getResources().getStringArray(R.array.color_names)[color];
			else
				name = lastStatus.getPlayerName(getResources(), data[i].player1, color);
			
			String s;
			((TextView)t[i].findViewById(R.id.name)).setText(name);
			t[i].findViewById(R.id.name).clearAnimation();
			
			((TextView)t[i].findViewById(R.id.place)).setText(String.format("%d.", data[i].place));

			s = getResources().getQuantityString(R.plurals.number_of_points, data[i].points, data[i].points);
			((TextView)t[i].findViewById(R.id.points)).setText(s);
			s = "";
			if (data[i].bonus > 0)
				s += " (+" + data[i].bonus + ")";
			
			((TextView)t[i].findViewById(R.id.bonus_points)).setText(s);
			
			((TextView)t[i].findViewById(R.id.stones)).setText(
					getResources().getQuantityString(R.plurals.number_of_stones_left, data[i].stones_left, data[i].stones_left));
				
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
			
			if (data[i].is_local) {
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
				
				this.place.setText(getResources().getStringArray(R.array.places)[data[i].place - 1]);
			}
			t[i].findViewById(R.id.data).startAnimation(set);
		}
	}
	
	Drawable getScoreDrawable(PlayerData data) {
		int color = Global.getPlayerColor(data.player1, spiel.m_gamemode);
		LayerDrawable l;
		
		if (data.player2 >= 0)
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_2);
		else
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_1);
		
		((GradientDrawable)l.findDrawableByLayerId(R.id.color1)).setColor(Global.PLAYER_BACKGROUND_COLOR[color]);
		if (data.player2 >= 0) {
			color = Global.getPlayerColor(data.player2, spiel.m_gamemode);
			((GradientDrawable)l.findDrawableByLayerId(R.id.color2)).setColor(Global.PLAYER_BACKGROUND_COLOR[color]);
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
		
		HighscoreDB db = new HighscoreDB(this);
		if (db.open()) {
			for (int i = 0; i < data.length; i++) if (data[i].is_local) {
					if (spiel.m_gamemode == Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS
							&& data[i].place == 1)
						getGamesClient().unlockAchievement(getString(R.string.achievement_win_blokus_classic));
					
					if (spiel.m_gamemode == Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS
							&& data[i].is_perfect)
						getGamesClient().unlockAchievement(getString(R.string.achievement_perfect_game));
					
					if (spiel.m_gamemode == Spielleiter.GAMEMODE_DUO
							&& data[i].place == 1)
						getGamesClient().unlockAchievement(getString(R.string.achievement_win_blokus_duo));
					
					getGamesClient().incrementAchievement(getString(R.string.achievement_1000_points), data[i].points);
				}

			getGamesClient().submitScore(
				getString(R.string.leaderboard_games_won),
				db.getNumberOfPlace(-1, 1));

			getGamesClient().submitScore(
				getString(R.string.leaderboard_points_total),
				db.getTotalNumberOfPoints(-1));

			db.close();
		} else
			throw new IllegalStateException("could not open highscore db");
	}
}
