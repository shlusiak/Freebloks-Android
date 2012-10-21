package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
	
	public void setOnNewGameListener(android.view.View.OnClickListener listener) {
		findViewById(R.id.new_game).setOnClickListener(listener);
	}
	
	public void setData(SpielClient spiel) {
		int place[] = { 0, 1, 2, 3 };
		TextView t[] = new TextView[4];
		final int colors[] = {
				Color.argb(96, 0, 0, 255),
				Color.argb(96, 255, 255, 0),
				Color.argb(96, 255, 0, 0),
				Color.argb(96, 0, 255, 0),
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
			if (spiel.get_player(place[i]).m_stone_points_left > spiel.get_player(place[i + 1]).m_stone_points_left) {
				int bla = place[i];
				place[i] = place[i + 1];
				place[i + 1] = bla;
				i = 0;
			}else i++;
		}
		
		t[0] = (TextView) findViewById(R.id.place1);
		t[1] = (TextView) findViewById(R.id.place2);
		t[2] = (TextView) findViewById(R.id.place3);
		t[3] = (TextView) findViewById(R.id.place4);
		
		for (i = 0; i < 4; i++) {
			Player p = spiel.get_player(place[i]);
			/* TODO: translate */
			t[i].setText(String.format("%s: -%d points (%d stones)",
					names[place[i]],
					p.m_stone_points_left,
					p.m_stone_count));
			t[i].setBackgroundColor(colors[place[i]]);
		}
	}
}
