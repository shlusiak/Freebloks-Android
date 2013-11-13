package de.saschahlusiak.freebloks.game;

import java.util.Arrays;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.stats.StatisticsActivity;
import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
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

public class GameFinishActivity extends Activity {
	public static final int RESULT_NEW_GAME = RESULT_FIRST_USER + 1;
	public static final int RESULT_SHOW_MENU = RESULT_FIRST_USER + 2;

	TextView place;
	NET_SERVER_STATUS lastStatus;
	String clientName;
	Spielleiter spiel;
	
	class AddToDBTask extends AsyncTask<PlayerData,Void,Void> {

		@Override
		protected Void doInBackground(PlayerData... data) {
			int local_players = 0;
			for (int i = 0; i < data.length; i++)
				if (data[i].is_local)
					local_players++;
			
			if (local_players != 1)
				return null;
			
			HighscoreDB db = new HighscoreDB(getBaseContext());
			if (db.open()) {
				for (int i = 0; i < data.length; i++) if (data[i].is_local) {
					int flags = 0;
					if (data[i].is_perfect)
						flags |= HighscoreDB.FLAG_IS_PERFECT;

					db.addHighscore(
							spiel.m_gamemode,
							data[i].points,
							data[i].stones_left,
							data[i].player1,
							data[i].place,
							flags);

					db.close();
					
					if (Build.VERSION.SDK_INT >= 8) {
						BackupManager backupManager = new BackupManager(getBaseContext());
						backupManager.dataChanged();
					}
				}
			}
			return null;
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.game_finish_activity);
		
	    
		place = (TextView) findViewById(R.id.place);
		
		spiel = (Spielleiter)getIntent().getSerializableExtra("game");
		lastStatus = (NET_SERVER_STATUS)getIntent().getSerializableExtra("lastStatus");
		clientName = getIntent().getStringExtra("clientName");

		
		PlayerData[] data = getData(spiel);
		updateViews(data, spiel.m_gamemode);
		if (savedInstanceState == null)
			new AddToDBTask().execute(data);
		
		
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
	}
	
	class PlayerData implements Comparable<PlayerData> {
		int player1, player2;
		int place;
		int points, stones_left;
		int bonus;
		boolean is_local, is_perfect;
		
		PlayerData(Spielleiter spiel, int player) {
			this.place = -1;
			this.player1 = player;
			this.player2 = -1;
			this.is_local = spiel.is_local_player(player);
			this.is_perfect = true;
			addPoints(spiel.get_player(player));
		}
		
		PlayerData(Spielleiter spiel, int player1, int player2) {
			this.place = -1;
			this.player1 = player1;
			this.player2 = player2;
			this.is_local = spiel.is_local_player(player1);
			this.is_perfect = true;
			
			addPoints(spiel.get_player(player1));
			addPoints(spiel.get_player(player2));
		}
		
		void addPoints(Player p) {
			this.points += p.m_stone_points;
			this.stones_left += p.m_stone_count;
			if (p.m_stone_count == 0 && p.m_lastStone != null) {
				if (p.m_lastStone.get_stone_shape() == 0) {
					bonus += 20;
				}
				else {
					bonus += 15;
					is_perfect = false;
				}
			} else
				is_perfect = false;
		}

		@Override
		public int compareTo(PlayerData another) {
			if (points > another.points)
				return -1;
			if (points < another.points)
				return 1;
			if (stones_left < another.stones_left)
				return -1;
			if (stones_left > another.stones_left)
				return 1;
			return 0;
		}
	}
	
	PlayerData[] getData(Spielleiter spiel) {
		PlayerData[] data;
		int i;
		switch (spiel.m_gamemode) {
		case Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS:
		case Spielleiter.GAMEMODE_DUO:
			data = new PlayerData[2];
			data[0] = new PlayerData(spiel, 0);
			data[1] = new PlayerData(spiel, 2);
			break;
			
		case Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS:
			data = new PlayerData[2];
			data[0] = new PlayerData(spiel, 0, 2);
			data[1] = new PlayerData(spiel, 1, 3);
			break;
			
		case Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS:
		default:
			data = new PlayerData[4];
			data[0] = new PlayerData(spiel, 0);
			data[1] = new PlayerData(spiel, 1);
			data[2] = new PlayerData(spiel, 2);
			data[3] = new PlayerData(spiel, 3);
			break;
		}
		
		Arrays.sort(data);
		int place;
		for (i = 0; i < data.length; i++) {
			place = i + 1;
			if (i > 0) {
				if (data[i].compareTo(data[i-1]) == 0)
					place = data[i-1].place;
			}
			
			data[i].place = place;
		}
		return data;
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
				
			t[i].findViewById(R.id.data).setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[color]);
			
			AnimationSet set = new AnimationSet(false);
			Animation a = new AlphaAnimation(0.0f, 1.0f);
			a.setStartOffset(i * 100);
			a.setDuration(600);
			a.setFillBefore(true);
			set.addAnimation(a);
			a = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_SELF, 
					-1, 
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
			t[i].startAnimation(set);
		}
	}
}
