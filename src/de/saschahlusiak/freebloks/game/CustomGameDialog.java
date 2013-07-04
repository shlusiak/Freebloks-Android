package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class CustomGameDialog extends Dialog implements OnSeekBarChangeListener {
	SeekBar difficulty;
	TextView difficulty_label;
	Spinner game_mode, field_size;

	final static int DIFFICULTY_MAX = 10; /* 0..10 = 11 */
	final static int DIFFICULTY_DEFAULT = 8;
	final static int DIFFICULTY_VALUES[] = {
		200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
	};
	final static int FIELD_SIZES[] = {
		13, 15, 17, 20, 23
	};

	CheckBox player1;
	CheckBox player2;
	CheckBox player3;
	CheckBox player4;

	public CustomGameDialog(Context context) {
		super(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		setContentView(R.layout.game_menu_new_custom_game);
		
		difficulty = (SeekBar)findViewById(R.id.difficulty);
		difficulty_label = (TextView)findViewById(R.id.difficulty_label);
		difficulty.setOnSeekBarChangeListener(this);
		difficulty.setMax(DIFFICULTY_MAX);
		int slider = DIFFICULTY_DEFAULT;
		int diff = prefs.getInt("difficulty", DIFFICULTY_VALUES[DIFFICULTY_DEFAULT]);
		for (int i = 0; i < DIFFICULTY_VALUES.length; i++)
			if (DIFFICULTY_VALUES[i] == diff)
				slider = i;
		difficulty.setProgress(slider);
		
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
		
		setLabel();
		
		setTitle(R.string.custom_game_title);
	}
	
	public void saveSettings() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		Editor editor = prefs.edit();
		editor.putInt("difficulty", getDifficulty());
		editor.commit();
	}
	
	public void prepare() {
		int p = (int)(Math.random() * 4.0);
		if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0) * 2;
		if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0);
		
		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);
		
		if (game_mode.getSelectedItemPosition() == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
			player3.setChecked(player1.isChecked());
			player4.setChecked(player2.isChecked());
		}
	}
	
	void setLabel() {
		final String labels[] = getContext().getResources().getStringArray(R.array.difficulties);
		int value = getDifficulty();
		int text = 0;
		
		if (value >= 5)
			text = 1;
		if (value >= 40)
			text = 2;
		if (value >= 80)
			text = 3;
		if (value >= 160)
			text = 4;
		difficulty_label.setText(String.format("%s (%d)", labels[text], difficulty.getProgress()));
	}
	
	public int getDifficulty() {
		return DIFFICULTY_VALUES[difficulty.getProgress()];
	}
	
	public int getGameMode() {
		return (int)game_mode.getSelectedItemPosition();
	}
	
	public int getFieldSize() {
		return FIELD_SIZES[field_size.getSelectedItemPosition()];
	}
	
	public boolean[] getPlayers() {
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

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		setLabel();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
}
