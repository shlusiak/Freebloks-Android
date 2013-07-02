package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class JoinDialog extends Dialog {
	
	public interface OnJoinListener {
		public boolean OnJoin(String server, boolean players[], String player_name);
	}
	
	CheckBox player1, player2, player3, player4;
	EditText name, server;

	public JoinDialog(Context context, final OnJoinListener listener) {
		super(context);
		setContentView(R.layout.join_game_dialog);
		
		setTitle(R.string.join_game);
		
		player1 = (CheckBox)findViewById(R.id.player1);
		player2 = (CheckBox)findViewById(R.id.player2);
		player3 = (CheckBox)findViewById(R.id.player3);
		player4 = (CheckBox)findViewById(R.id.player4);
		
		name = (EditText)findViewById(R.id.name);
		server = (EditText)findViewById(R.id.server);
		
		server.setText("blokus.mooo.com");
		Button okAdd = (Button)findViewById(android.R.id.button1);
		okAdd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String s = server.getText().toString();
				String n = name.getText().toString();

				SharedPreferences prefs;
				prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				Editor editor = prefs.edit();
				editor.putString("player_name", n);
				editor.commit();
				
				if (listener.OnJoin(s, getPlayers(), n))
					dismiss();
			}
		});
		((Button)findViewById(android.R.id.button2)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public void prepare() {
		int p = (int)(Math.random() * 4.0);
		
		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);
		
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		name.setText(prefs.getString("player_name", null));
	}
	
	boolean[] getPlayers() {
		boolean p[] = new boolean[4];
		p[0] = player1.isChecked();
		p[1] = player2.isChecked();
		p[2] = player3.isChecked();
		p[3] = player4.isChecked();				
		return p;
	}
}
