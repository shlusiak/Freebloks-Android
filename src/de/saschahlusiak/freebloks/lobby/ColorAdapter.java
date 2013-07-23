package de.saschahlusiak.freebloks.lobby;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.GridLayout.LayoutParams;
import android.widget.GridView;
import android.widget.TextView;

public class ColorAdapter extends BaseAdapter {
	Context context;
	NET_SERVER_STATUS lastStatus;
	Spielleiter spiel;
	
	public ColorAdapter(Context context, Spielleiter spiel, NET_SERVER_STATUS lastStatus) {
		this.context = context;
		this.lastStatus = lastStatus;
		this.spiel = spiel;
	}
	
	void setCurrentStatus(Spielleiter spiel, NET_SERVER_STATUS status) {
		this.spiel = spiel;
		this.lastStatus = status;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (lastStatus != null && lastStatus.gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS)
			return 2;
		return 4;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = LayoutInflater.from(context).inflate(R.layout.color_grid_item, parent, false);
		TextView t;
		GridView.LayoutParams lp = new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		t = (TextView)v.findViewById(R.id.text);
        v.setLayoutParams(lp);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        if (lastStatus == null || spiel == null) {
			t.setTextColor(Global.PLAYER_FOREGROUND_COLOR[position]);
			t.setBackgroundColor(Color.BLACK);
        	t.setText("---");
        	return v;
        }        
		
		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if (lastStatus.gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS)
			if (position == 1)
				position = 2;
		
		if (lastStatus.isAdvanced()) {
			if (lastStatus.spieler[position] >= 0) {
				/* it is a human player */
				if (lastStatus.client_names[lastStatus.spieler[position]] == null)
					t.setText(context.getString(R.string.client_d, lastStatus.spieler[position]));
				else
					t.setText(lastStatus.client_names[lastStatus.spieler[position]]);
		        v.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[position]);
				if (spiel.is_local_player(position)) {
					t.setTypeface(Typeface.DEFAULT_BOLD);
					
					Animation a = new TranslateAnimation(
							TranslateAnimation.RELATIVE_TO_SELF, 
							0, 
							TranslateAnimation.RELATIVE_TO_SELF, 
							0, 
							TranslateAnimation.RELATIVE_TO_SELF, 
							0.15f, 
							TranslateAnimation.RELATIVE_TO_SELF, 
							-0.15f);
					a.setDuration(400);
					a.setInterpolator(new DecelerateInterpolator());
					a.setRepeatMode(Animation.REVERSE);
					a.setRepeatCount(Animation.INFINITE);

					t.startAnimation(a);
				}
			} else {
				/* computer player */
				t.setTextColor(Global.PLAYER_FOREGROUND_COLOR[position]);
				t.setBackgroundColor(Color.BLACK);
				t.setText("---");
			}
		} else {
			if (spiel.is_local_player(position)) {
		        v.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[position]);
				final String colorNames[] = context.getResources().getStringArray(R.array.color_names);
				t.setText(colorNames[position]);

				t.setTypeface(Typeface.DEFAULT_BOLD);
				
				Animation a = new TranslateAnimation(
						TranslateAnimation.RELATIVE_TO_SELF, 
						0, 
						TranslateAnimation.RELATIVE_TO_SELF, 
						0, 
						TranslateAnimation.RELATIVE_TO_SELF, 
						0.15f, 
						TranslateAnimation.RELATIVE_TO_SELF, 
						-0.15f);
				a.setDuration(400);
				a.setInterpolator(new DecelerateInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);

				t.startAnimation(a);
			} else {
				t.setTextColor(Global.PLAYER_FOREGROUND_COLOR[position]);
				t.setBackgroundColor(Color.BLACK);
				t.setText("---");
			}
		}
        
		return v;
	}
}
