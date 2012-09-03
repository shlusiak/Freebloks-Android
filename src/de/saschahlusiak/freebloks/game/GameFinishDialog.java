package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import android.app.Dialog;
import android.content.Context;
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
			t[i].setText(String.format("Player %d: -%d points (%d stones)", place[i], p.m_stone_points_left, p.m_stone_count));
		}
	}
}
