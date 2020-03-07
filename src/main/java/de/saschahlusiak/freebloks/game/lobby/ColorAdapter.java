package de.saschahlusiak.freebloks.game.lobby;

import android.widget.CheckBox;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Game;
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

import androidx.annotation.Nullable;

@Deprecated
public class ColorAdapter extends BaseAdapter {
	interface EditPlayerNameListener {
		void onEditPlayerName(int player);
	}

	private Context context;
	private MessageServerStatus lastStatus;
	private Game game;
	private EditPlayerNameListener listener;

	public ColorAdapter(EditPlayerNameListener listener, Context context, Game game, MessageServerStatus lastStatus) {
		this.listener = listener;
		this.context = context;
		this.lastStatus = lastStatus;
		this.game = game;
	}

	void setCurrentStatus(Game game, @Nullable MessageServerStatus status) {
		this.game = game;
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
		if (lastStatus == null || game == null)
			return false;

		/* if in two player mode, we have only 2 positions, make player 1 (yellow) the player 2 (red) */
		if (lastStatus.getGameMode() == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_DUO ||
			lastStatus.getGameMode() == GameMode.GAMEMODE_JUNIOR)
			if (position == 1)
				position = 2;

		if (game.isStarted())
			return false;

		if (lastStatus.isClient(position)) {
			/* it is a human player */
			if (game.isLocalPlayer(position))
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
        if (lastStatus == null || game == null) {
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

		v.findViewById(R.id.editButton).setOnClickListener(view -> listener.onEditPlayerName(player));

		background.setColor(context.getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[Global.getPlayerColor(player, game.getGameMode())]));

		if (lastStatus.isClient(player)) {
			/* it is a human player */
			t.setText(lastStatus.getClientName(context.getResources(), lastStatus.getClient(player)));
			v.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
			if (game.isLocalPlayer(player)) {
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
			if (game.isStarted()) {
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

		return v;
	}
}
