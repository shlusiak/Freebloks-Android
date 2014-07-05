package de.saschahlusiak.freebloks.lobby;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.opengl.Visibility;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
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
		if (lastStatus != null && lastStatus.gamemode == Spielleiter.GAMEMODE_DUO)
			return 2;
		return 4;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (lastStatus == null)
			return position;
		if (lastStatus.gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
				lastStatus.gamemode == Spielleiter.GAMEMODE_DUO)
				if (position == 1)
					position = 2;
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		if (lastStatus == null || spiel == null)
			return false;

		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if (lastStatus.gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.gamemode == Spielleiter.GAMEMODE_DUO)
			if (position == 1)
				position = 2;

		if (!lastStatus.isAdvanced())
			return false;

		if (spiel.isStarted())
			return false;

		if (lastStatus.spieler[position] >= 0) {
			/* it is a human player */
			if (spiel.is_local_player(position))
				return true;
		} else
			return true;

		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null)
			v = LayoutInflater.from(context).inflate(R.layout.color_grid_item, parent, false);
		else
			v = convertView;

		TextView t;

		LayerDrawable ld = (LayerDrawable)context.getResources().getDrawable(R.drawable.bg_card_1);
		GradientDrawable background = ((GradientDrawable)ld.findDrawableByLayerId(R.id.color1));
		v.setBackgroundDrawable(ld);

		t = (TextView)v.findViewById(R.id.text);
		t.setTextColor(Color.WHITE);
		t.setVisibility(View.VISIBLE);
        if (lastStatus == null || spiel == null) {
        	/* unknown game state */
			background.setColor(Color.BLACK);
			background.setAlpha(96);
        	t.setText("---");
        	t.clearAnimation();
        	v.findViewById(R.id.progressBar).setVisibility(View.GONE);
        	return v;
        }

		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if (lastStatus.gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.gamemode == Spielleiter.GAMEMODE_DUO)
			if (position == 1)
				position = 2;

		if (lastStatus.isAdvanced()) {
			if (lastStatus.spieler[position] >= 0) {
				/* it is a human player */
				t.setText(lastStatus.getClientName(context.getResources(), lastStatus.spieler[position]));
		        background.setColor(Global.PLAYER_BACKGROUND_COLOR[Global.getPlayerColor(position, spiel.m_gamemode)]);
	        	v.findViewById(R.id.progressBar).setVisibility(View.GONE);
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
				} else {
		        	t.clearAnimation();
				}
			} else {
				/* computer player */
				background.setColor(Global.PLAYER_BACKGROUND_COLOR[Global.getPlayerColor(position, spiel.m_gamemode)]);
				background.setAlpha(96);
				t.setText("---");
				t.clearAnimation();
				if (spiel.isStarted()) {
					t.setVisibility(View.VISIBLE);
					v.findViewById(R.id.progressBar).setVisibility(View.GONE);
				} else {
					v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
					t.setVisibility(View.INVISIBLE);
				}
			}
		} else {
			if (spiel.is_local_player(position)) {
	        	v.findViewById(R.id.progressBar).setVisibility(View.GONE);
		        background.setColor(Global.PLAYER_BACKGROUND_COLOR[Global.getPlayerColor(position, spiel.m_gamemode)]);
				final String colorNames[] = context.getResources().getStringArray(R.array.color_names);
				t.setText(colorNames[position + 1]);

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
	        	v.findViewById(R.id.progressBar).setVisibility(View.GONE);
				background.setColor(Global.PLAYER_BACKGROUND_COLOR[Global.getPlayerColor(position, spiel.m_gamemode)]);
				background.setAlpha(96);
				t.setText("---");
				t.clearAnimation();
			}
		}

		return v;
	}
}
