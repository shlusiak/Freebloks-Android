package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class JoinDialog extends Dialog {
	
	public interface OnJoinListener {
		public boolean OnJoin(String server, boolean players[], int game_mode, int field_size, String player_name);
	}
	
	final static int FIELD_SIZES[] = {
		13, 15, 17, 20, 23
	};
	
	CheckBox player1, player2, player3, player4;
	EditText name, server;
	Spinner field_size, game_mode;

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

		field_size = (Spinner) findViewById(R.id.field_size);
		field_size.setSelection(3);
		
		game_mode = (Spinner) findViewById(R.id.game_mode);
		game_mode.setSelection(Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS);
		
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
				
				if (listener.OnJoin(s, getPlayers(), getGameMode(), getFieldSize(), n))
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
		findViewById(R.id.spinners).setVisibility(View.GONE);
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
		findViewById(R.id.spinners).setVisibility(View.VISIBLE);
		setTitle(R.string.host_game);
		
		game_mode.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {				
				if (position == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS) {	
					player1.setEnabled(true);
					player3.setEnabled(true);
					player2.setEnabled(false);
					player4.setEnabled(false);

					if (player2.isChecked())
						player1.setChecked(true);
					if (player4.isChecked())
						player3.setChecked(true);
					player2.setChecked(false);
					player4.setChecked(false);
					field_size.setSelection(1); /* 15x15 */
				} else if (position == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
					player1.setEnabled(true);
					player2.setEnabled(true);
					player3.setEnabled(false);
					player4.setEnabled(false);

					boolean e;
					e = player1.isChecked() || player3.isChecked();
					player1.setChecked(e);
					player3.setChecked(e);

					e = player2.isChecked() || player4.isChecked();
					player2.setChecked(e);
					player4.setChecked(e);
				} else {
					player1.setEnabled(true);
					player2.setEnabled(true);
					player3.setEnabled(true);
					player4.setEnabled(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		player1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
					player3.setChecked(player1.isChecked());
				}
			}
		});
		player2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
					player4.setChecked(player2.isChecked());
				}
			}
		});
	}

	boolean[] getPlayers() {
		boolean p[] = new boolean[4];
		p[0] = player1.isChecked();
		p[1] = player2.isChecked();
		p[2] = player3.isChecked();
		p[3] = player4.isChecked();
		if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
			/* this would otherwise request players two times, the server would hand out 2x2 = 4 players */
			p[2] = p[3] = false;
		}
		return p;
	}
	
	public int getGameMode() {
		return (int)game_mode.getSelectedItemPosition();
	}
	
	public int getFieldSize() {
		return FIELD_SIZES[field_size.getSelectedItemPosition()];
	}

}
