package de.saschahlusiak.freebloks.lobby;

import android.widget.CheckBox;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.model.Spielleiter;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ColorAdapter extends BaseAdapter {
	private Context context;
	private MessageServerStatus lastStatus;
	private Spielleiter spiel;
	private LobbyDialog lobby;

	public ColorAdapter(LobbyDialog lobby, Context context, Spielleiter spiel, MessageServerStatus lastStatus) {
		this.lobby = lobby;
		this.context = context;
		this.lastStatus = lastStatus;
		this.spiel = spiel;
	}

	void setCurrentStatus(Spielleiter spiel, MessageServerStatus status) {
		this.spiel = spiel;
		this.lastStatus = status;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (lastStatus != null && lastStatus.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS)
			return 2;
		if (lastStatus != null && lastStatus.getGameMode() == GameMode.GAMEMODE_DUO)
			return 2;
		if (lastStatus != null && lastStatus.getGameMode() == GameMode.GAMEMODE_JUNIOR)
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
		if (lastStatus.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_DUO ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_JUNIOR)
				if (position == 1)
					position = 2;
		return position;
	}

	@Override
	public boolean isEnabled(int position) {
		if (lastStatus == null || spiel == null)
			return false;

		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if (lastStatus.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_DUO ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_JUNIOR)
			if (position == 1)
				position = 2;

		if (!lastStatus.isVersion(2))
			return false;

		if (spiel.isStarted())
			return false;

		if (lastStatus.getSpieler()[position] >= 0) {
			/* it is a human player */
			if (spiel.is_local_player(position))
				return true;
		} else
			return true;

		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View v;
		if (convertView == null)
			v = LayoutInflater.from(context).inflate(R.layout.color_grid_item, parent, false);
		else
			v = convertView;

		TextView t;
		CheckBox c = v.findViewById(R.id.checkBox);

		LayerDrawable ld = (LayerDrawable)context.getResources().getDrawable(R.drawable.bg_card_1).mutate();
		GradientDrawable background = ((GradientDrawable)ld.findDrawableByLayerId(R.id.color1));
		v.setBackgroundDrawable(ld);

		t = v.findViewById(R.id.text);
		t.setTextColor(Color.WHITE);
		t.setVisibility(View.VISIBLE);
        if (lastStatus == null || spiel == null) {
        	/* unknown game state */
			background.setColor(Color.BLACK);
			background.setAlpha(96);
        	t.setText("---");
        	t.clearAnimation();
        	v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
			v.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);
			c.setChecked(false);

			return v;
        }

        final int player;
		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if ((lastStatus.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_JUNIOR ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_DUO) && position == 1)
				player = 2;
		else player = position;

		v.findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
		   @Override
		   public void onClick(View view) {
			   lobby.editPlayerName(player);
		   }
	   });

		background.setColor(context.getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[Global.getPlayerColor(player, spiel.getGameMode())]));
		if (lastStatus.isVersion(2)) {
			if (lastStatus.getSpieler()[player] >= 0) {
				/* it is a human player */
				t.setText(lastStatus.getClientName(context.getResources(), lastStatus.getSpieler()[player]));
	        	v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				if (spiel.is_local_player(player)) {
					t.setTypeface(Typeface.DEFAULT_BOLD);
					v.findViewById(R.id.editButton).setVisibility(View.VISIBLE);

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

					c.setChecked(true);
					c.setEnabled(true);
					c.setVisibility(View.VISIBLE);
				} else {
		        	t.clearAnimation();
					v.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);
					c.setChecked(false);
					c.setEnabled(false);
					c.setVisibility(View.INVISIBLE);
				}
			} else {
				/* computer player */
				background.setAlpha(96);
				t.setText("---");
				t.clearAnimation();
				if (spiel.isStarted()) {
					t.setVisibility(View.VISIBLE);
					v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				} else {
					v.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
					t.setVisibility(View.INVISIBLE);
				}
				v.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);
				c.setChecked(false);
				c.setEnabled(false);
				c.setVisibility(View.VISIBLE);
			}
		} else {
			if (spiel.is_local_player(player)) {
	        	v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				final String colorNames[] = context.getResources().getStringArray(R.array.color_names);
				t.setText(colorNames[player + 1]);

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
				v.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);
				c.setVisibility(View.VISIBLE);
				c.setChecked(true);
			} else {
	        	v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				background.setAlpha(96);
				t.setText("---");
				t.clearAnimation();
				v.findViewById(R.id.editButton).setVisibility(View.INVISIBLE);
				c.setVisibility(View.INVISIBLE);
			}
		}

		return v;
	}
}
