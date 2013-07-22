package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
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
		
		player1 = (CheckBox)findViewById(R.id.player1);
		player2 = (CheckBox)findViewById(R.id.player2);
		player3 = (CheckBox)findViewById(R.id.player3);
		player4 = (CheckBox)findViewById(R.id.player4);
		
		player1.setText(context.getResources().getStringArray(R.array.color_names)[0]);
		player2.setText(context.getResources().getStringArray(R.array.color_names)[1]);
		player3.setText(context.getResources().getStringArray(R.array.color_names)[2]);
		player4.setText(context.getResources().getStringArray(R.array.color_names)[3]);

		
		name = (EditText)findViewById(R.id.name);
		server = (EditText)findViewById(R.id.server);
		
		server.setText(Global.DEFAULT_SERVER_ADDRESS);
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
	
	public void prepareJoinDialog() {
		int p = (int)(Math.random() * 4.0);
		
		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);
		
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		name.setText(prefs.getString("player_name", null));
		findViewById(R.id.linearLayout1).setVisibility(View.VISIBLE);
		setTitle(R.string.join_game);		
	}

	public void prepareHostDialog() {
		int p = (int)(Math.random() * 4.0);
		
		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);
		
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		name.setText(prefs.getString("player_name", null));
		findViewById(R.id.linearLayout1).setVisibility(View.GONE);
		setTitle(R.string.host_game);		
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
