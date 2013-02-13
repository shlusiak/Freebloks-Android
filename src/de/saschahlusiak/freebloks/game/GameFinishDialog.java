package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class GameFinishDialog extends Dialog {
	
	public GameFinishDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_finish_dialog);
		
		setTitle(R.string.game_finished);
	}
	
	public void setData(SpielClient spiel) {
		int place[] = { 0, 1, 2, 3 };
		ViewGroup t[] = new ViewGroup[4];
		/* TODO: generalize */
		final int colors[] = {
				Color.rgb(0, 0, 96),
				Color.rgb(128, 128, 0),
				Color.rgb(96, 0, 0),
				Color.rgb(0, 96, 0),
		};
		/* TODO: translate */
		final String names[] = {
				"Blue",
				"Yellow",
				"Red",
				"Green"
		};
		
		int i = 0;
		while ( i < Spiel.PLAYER_MAX - 1)
		{
			if (spiel.spiel.get_player(place[i]).m_stone_points_left > spiel.spiel.get_player(place[i + 1]).m_stone_points_left) {
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
		
		for (i = 0; i < 4; i++) {
			String name;
			Player p = spiel.spiel.get_player(place[i]);
			/* TODO: translate */
			name = names[place[i]];
			
			((TextView)t[i].findViewById(R.id.name)).setText(name);
			((TextView)t[i].findViewById(R.id.points)).setText(String.format("-%d points", p.m_stone_points_left));
			((TextView)t[i].findViewById(R.id.stones)).setText(String.format("%d stones", p.m_stone_count));
			t[i].setBackgroundColor(colors[place[i]]);
			
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
			
			if (spiel.spiel.is_local_player(place[i])) {
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

				(t[i].findViewById(R.id.name)).startAnimation(a);
			}
			t[i].startAnimation(set);
			setTitle(String.format("Place %d", i + 1));
		}
	}
}
