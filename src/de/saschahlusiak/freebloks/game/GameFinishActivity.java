package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.stats.StatisticsActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.game_finish_activity);
		
	    
		place = (TextView) findViewById(R.id.place);
		
		Spielleiter spiel = (Spielleiter)getIntent().getSerializableExtra("game");
		lastStatus = (NET_SERVER_STATUS)getIntent().getSerializableExtra("lastStatus");
		clientName = getIntent().getStringExtra("clientName");

		int local_players = 0;
		for (int i = 0; i < Spiel.PLAYER_MAX; i++)
			if (spiel.is_local_player(i))
				local_players++;

		boolean addToDB = false;
		if (local_players == 1)
			addToDB = true;
		/* TODO: point counting for 4_COLORS_2_PLAYERS is currently just wrong. Don't add to DB yet */
//		if (local_players == 2 && spiel.m_gamemode == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS)
//			addToDB = true;
		if (savedInstanceState != null)
			addToDB = false;
		setData(spiel, addToDB);
		
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
	
	public void setData(Spielleiter spiel, boolean addToDB) {
		int place[] = { 0, 1, 2, 3 };
		ViewGroup t[] = new ViewGroup[4];
		
		if (spiel == null)
			return;

		int i = 0;
		int max = Spiel.PLAYER_MAX - 1;
		if (spiel.m_gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
			spiel.m_gamemode == Spielleiter.GAMEMODE_DUO) {
			place[1] = 2;
			place[2] = 1;
			max = 1;
		}
		while ( i < max )
		{
			if (spiel.get_player(place[i]).m_stone_points < spiel.get_player(place[i + 1]).m_stone_points) {
				int bla = place[i];
				place[i] = place[i + 1];
				place[i + 1] = bla;
				i = 0;
			}else i++;
		}
		
		t[0] = (ViewGroup) findViewById(R.id.place1);
		t[1] = (ViewGroup) findViewById(R.id.place2);
		t[2] = (ViewGroup) findViewById(R.id.place3);
		t[3] = (ViewGroup) findViewById(R.id.place4);
		
		/* TODO: combine yellow/green, blue/red on 4_COLORS_2_PLAYERS */
		if (spiel.m_gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
			spiel.m_gamemode == Spielleiter.GAMEMODE_DUO) {
			t[2].setVisibility(View.GONE);
			t[3].setVisibility(View.GONE);
		} else {
			t[2].setVisibility(View.VISIBLE);
			t[3].setVisibility(View.VISIBLE);
		}

		this.place.setText(R.string.game_finished);

		for (i = 3; i >= 0; i--) {
			String name;
			int color = Global.getPlayerColor(place[i], spiel.m_gamemode);
			Player p = spiel.get_player(place[i]);
			if (clientName != null && spiel.is_local_player(place[i]))
				name = clientName; 
			else if (lastStatus == null)
				name = getResources().getStringArray(R.array.color_names)[color];
			else
				name = lastStatus.getPlayerName(getResources(), place[i], color);
			
			String s;
			((TextView)t[i].findViewById(R.id.name)).setText(name);
			t[i].findViewById(R.id.name).clearAnimation();
			
			s = getResources().getQuantityString(R.plurals.number_of_points, p.m_stone_points, p.m_stone_points);
			((TextView)t[i].findViewById(R.id.points)).setText(s);
			s = "";
			boolean is_perfect = false;
			if (p.m_stone_count == 0 && p.m_lastStone != null) {
				if (p.m_lastStone.get_stone_shape() == 0) {
					is_perfect = true;
					s = "(+20)";
				}
				else
					s = "(+15)";
			}
			((TextView)t[i].findViewById(R.id.bonus_points)).setText(s);
			
			((TextView)t[i].findViewById(R.id.stones)).setText(
					getResources().getQuantityString(R.plurals.number_of_stones_left, p.m_stone_count, p.m_stone_count));
				
			t[i].setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[color]);
			
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
			
			if (spiel.is_local_player(place[i])) {
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
				((TextView)t[i].findViewById(R.id.name)).setTypeface(Typeface.DEFAULT_BOLD);
				((TextView)t[i].findViewById(R.id.stones)).setTextColor(Color.WHITE);

				t[i].findViewById(R.id.name).startAnimation(a);
				
				a = new AlphaAnimation(0.5f, 1.0f);
				a.setDuration(750);
				a.setInterpolator(new LinearInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);
				
				set.addAnimation(a);
				
				this.place.setText(getResources().getStringArray(R.array.places)[i]);

				if (addToDB) {
					HighscoreDB db = new HighscoreDB(this);
					db.open();
					
					int flags = 0;
					if (is_perfect)
						flags |= HighscoreDB.FLAG_IS_PERFECT;
					
					/* FIXME: this is wrong for 4_COLORS_2_PLAYERS */
					db.addHighscore(
							spiel.m_gamemode,
							p.m_stone_points,
							p.m_stone_count,
							place[i],
							i + 1,
							flags);
					
					db.close();
				}
			}
			t[i].startAnimation(set);
		}
	}
}
