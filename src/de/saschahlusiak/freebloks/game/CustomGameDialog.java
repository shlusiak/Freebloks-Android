package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloksvip.R;
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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

public class CustomGameDialog extends Dialog implements OnSeekBarChangeListener {
	final static int DIFFICULTY_MAX = 10; /* 0..10 = 11 */
	final static int DIFFICULTY_DEFAULT = 8;
	final static int DIFFICULTY_VALUES[] = {
		200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
	};
	final static int FIELD_SIZES[] = {
		13, 14, 15, 17, 20, 23
	};

	CheckBox player1;
	CheckBox player2;
	CheckBox player3;
	CheckBox player4;

	SeekBar difficulty;
	TextView difficulty_label;
	Spinner game_mode, field_size;

	EditText name, server;
	OnStartCustomGameListener listener;


	public interface OnStartCustomGameListener {
		public boolean OnStart(CustomGameDialog dialog);
	}

	public CustomGameDialog(Context context, final OnStartCustomGameListener listener) {
		super(context);

		this.listener = listener;

		setContentView(R.layout.game_menu_new_custom_game);

		difficulty = (SeekBar)findViewById(R.id.difficulty);
		difficulty_label = (TextView)findViewById(R.id.difficulty_label);
		difficulty.setOnSeekBarChangeListener(this);
		difficulty.setMax(DIFFICULTY_MAX);

		player1 = (CheckBox)findViewById(R.id.player1);
		player2 = (CheckBox)findViewById(R.id.player2);
		player3 = (CheckBox)findViewById(R.id.player3);
		player4 = (CheckBox)findViewById(R.id.player4);

		field_size = (Spinner) findViewById(R.id.field_size);

		game_mode = (Spinner) findViewById(R.id.game_mode);
		game_mode.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				if (position == Spielleiter.GAMEMODE_DUO) {
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
					/* FIXME: on first create this is called after prepare, which does seem to not persiste the
					 * last set size if != 14 */
					field_size.setSelection(1); /* 14x14 */
				} else if (position == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS) {
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
					/* FIXME: on first create this is called after prepare, which does seem to not persiste the
					 * last set size if != 15 */
					field_size.setSelection(2); /* 15x15 */
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
				updateNames();
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
		findViewById(android.R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSettings();
				if (listener.OnStart(CustomGameDialog.this))
					dismiss();
			}
		});

		name = (EditText)findViewById(R.id.name);
		server = (EditText)findViewById(R.id.server);
		server.setText(Global.DEFAULT_SERVER_ADDRESS);

		updateNames();

		setDifficultyLabel();
	}

	void updateNames() {
		String[] color_names = getContext().getResources().getStringArray(R.array.color_names);
		int game_mode = this.game_mode.getSelectedItemPosition();
		player1.setText(color_names[Global.getPlayerColor(0, game_mode)]);
		player2.setText(color_names[Global.getPlayerColor(1, game_mode)]);
		player3.setText(color_names[Global.getPlayerColor(2, game_mode)]);
		player4.setText(color_names[Global.getPlayerColor(3, game_mode)]);
	}

	private void saveSettings() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		Editor editor = prefs.edit();
		editor.putInt("difficulty", getDifficulty());
		editor.putString("player_name", getName());
		editor.putInt("gamemode", getGameMode());
		editor.putInt("fieldsize", getFieldSize());
		editor.commit();
	}

	private void prepare(String name, int difficulty, int gamemode, int fieldsize) {
		int p = (int)(Math.random() * 4.0);
		game_mode.setSelection(gamemode);

		if (gamemode == Spielleiter.GAMEMODE_2_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0) * 2;
		if (gamemode == Spielleiter.GAMEMODE_DUO)
			p = (int)(Math.random() * 2.0) * 2;
		if (gamemode == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0);

		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);

		if (gamemode == Spielleiter.GAMEMODE_4_COLORS_2_PLAYERS) {
			player3.setChecked(player1.isChecked());
			player4.setChecked(player2.isChecked());
		}

		this.name.setText(name);
		updateNames();

		int slider = DIFFICULTY_DEFAULT;
		for (int i = 0; i < DIFFICULTY_VALUES.length; i++)
			if (DIFFICULTY_VALUES[i] == difficulty)
				slider = i;
		this.difficulty.setProgress(slider);

		slider = 3;
		for (int i = 0; i < FIELD_SIZES.length; i++)
			if (FIELD_SIZES[i] == fieldsize)
				slider = i;
		field_size.setSelection(slider);
	}

	void prepareCustomGameDialog(String name, int difficulty, int gamemode, int fieldsize) {
		prepare(name, difficulty, gamemode, fieldsize);
		setTitle(R.string.custom_game_title);
		findViewById(R.id.player_name_layout).setVisibility(View.GONE);
		findViewById(R.id.server_address_layout).setVisibility(View.GONE);
	}

	void prepareJoinDialog(String name, int difficulty, int gamemode, int fieldsize) {
		/* NOTE: if the spinner is hidden, the callback to enable the colors is not called! */
		prepare(name, difficulty, gamemode, fieldsize);
		setTitle(R.string.join_game);
		findViewById(R.id.difficulty_layout).setVisibility(View.GONE);
		findViewById(R.id.spinner_layout).setVisibility(View.GONE);
	}

	void prepareHostDialog(String name, int difficulty, int gamemode, int fieldsize) {
		prepare(name, difficulty, gamemode, fieldsize);
		setTitle(R.string.host_game);
		findViewById(R.id.difficulty_layout).setVisibility(View.GONE);
		findViewById(R.id.server_address_layout).setVisibility(View.GONE);
	}

	void setDifficultyLabel() {
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
		setDifficultyLabel();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	public String getName() {
		return name.getText().toString();
	}

	public String getServer() {
		return server.getText().toString();
	}
}
