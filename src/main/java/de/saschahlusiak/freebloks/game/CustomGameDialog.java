package de.saschahlusiak.freebloks.game;

import android.view.WindowManager;
import com.shawnlin.numberpicker.NumberPicker;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.model.GameConfig;
import de.saschahlusiak.freebloks.model.GameMode;
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

import de.saschahlusiak.freebloks.model.Shape;

public class CustomGameDialog extends Dialog implements OnSeekBarChangeListener, View.OnClickListener, OnItemSelectedListener {
	public interface OnStartCustomGameListener {
		boolean OnStart(GameConfig config);
	}

	private final static int DIFFICULTY_MAX = 10; /* 0..10 = 11 */

	private final static int DIFFICULTY_DEFAULT = 8;

	private final static int[] DIFFICULTY_VALUES = {
		200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
	};

	private CheckBox player1;
	private CheckBox player2;
	private CheckBox player3;
	private CheckBox player4;

	private SeekBar difficulty;
	private TextView difficulty_label;
	private Spinner gameMode, fieldSize;
	private NumberPicker[] picker;

	private OnStartCustomGameListener listener;

	public CustomGameDialog(Context context, final OnStartCustomGameListener listener) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);

		this.listener = listener;

		setContentView(R.layout.custom_game_dialog);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		difficulty = findViewById(R.id.difficulty);
		difficulty_label = findViewById(R.id.difficulty_label);
		difficulty.setOnSeekBarChangeListener(this);
		difficulty.setMax(DIFFICULTY_MAX);

		player1 = findViewById(R.id.player1);
		player2 = findViewById(R.id.player2);
		player3 = findViewById(R.id.player3);
		player4 = findViewById(R.id.player4);

		picker = new NumberPicker[5];
		picker[0] = findViewById(R.id.picker1);
		picker[1] = findViewById(R.id.picker2);
		picker[2] = findViewById(R.id.picker3);
		picker[3] = findViewById(R.id.picker4);
		picker[4] = findViewById(R.id.picker5);

		fieldSize = findViewById(R.id.field_size);

		findViewById(R.id.advanced).setOnClickListener(this);

		gameMode = findViewById(R.id.game_mode);
		gameMode.setOnItemSelectedListener(this);

		player1.setOnClickListener(this);
		player2.setOnClickListener(this);

		findViewById(R.id.cancel).setOnClickListener(this);
		findViewById(R.id.ok).setOnClickListener(this);

		updateNames();

		setDifficultyLabel();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
			case R.id.cancel:
				dismiss();
				break;

			case R.id.ok:
				saveSettings();
				if (listener.OnStart(getConfiguration()))
					dismiss();
				break;

			case R.id.advanced:
				findViewById(R.id.advanced).setVisibility(View.GONE);
				findViewById(R.id.custom_stones_layout).setVisibility(View.VISIBLE);
				break;

			case R.id.player1:
				if (gameMode.getSelectedItemPosition() == GameMode.GAMEMODE_4_COLORS_2_PLAYERS.ordinal()) {
					player3.setChecked(player1.isChecked());
				}
				break;

			case R.id.player2:
				if (gameMode.getSelectedItemPosition() == GameMode.GAMEMODE_4_COLORS_2_PLAYERS.ordinal()) {
					player4.setChecked(player2.isChecked());
				}
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (position == GameMode.GAMEMODE_DUO.ordinal() || position == GameMode.GAMEMODE_JUNIOR.ordinal()) {
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
			fieldSize.setSelection(1); /* 14x14 */
		} else if (position == GameMode.GAMEMODE_2_COLORS_2_PLAYERS.ordinal()) {
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
			/* FIXME: on first create this is called after prepare, which does seem to not persist the
			 * last set size if != 15 */
			fieldSize.setSelection(2); /* 15x15 */
		} else if (position == GameMode.GAMEMODE_4_COLORS_2_PLAYERS.ordinal()) {
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
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void updateNames() {
		String[] color_names = getContext().getResources().getStringArray(R.array.color_names);
		GameMode game_mode = GameMode.from(this.gameMode.getSelectedItemPosition());
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
		editor.putInt("gamemode", getGameMode().ordinal());
		editor.putInt("fieldsize", getFieldSize());
		editor.apply();
	}

	private void prepare(int difficulty, GameMode gamemode, int fieldsize) {
		int p = (int)(Math.random() * 4.0);
		gameMode.setSelection(gamemode.ordinal());

		if (gamemode == GameMode.GAMEMODE_2_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0) * 2;
		if (gamemode == GameMode.GAMEMODE_DUO || gamemode == GameMode.GAMEMODE_JUNIOR)
			p = (int)(Math.random() * 2.0) * 2;
		if (gamemode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS)
			p = (int)(Math.random() * 2.0);

		player1.setChecked(p == 0);
		player2.setChecked(p == 1);
		player3.setChecked(p == 2);
		player4.setChecked(p == 3);

		if (gamemode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
			player3.setChecked(player1.isChecked());
			player4.setChecked(player2.isChecked());
		}

		updateNames();

		int slider = DIFFICULTY_DEFAULT;
		for (int i = 0; i < DIFFICULTY_VALUES.length; i++)
			if (DIFFICULTY_VALUES[i] == difficulty)
				slider = i;
		this.difficulty.setProgress(slider);

		slider = 3;
		for (int i = 0; i < GameConfig.FIELD_SIZES.length; i++)
			if (GameConfig.FIELD_SIZES[i] == fieldsize)
				slider = i;

		fieldSize.setSelection(slider);

		findViewById(R.id.advanced).setVisibility(View.VISIBLE);
		findViewById(R.id.custom_stones_layout).setVisibility(View.GONE);
	}

	void prepareCustomGameDialog(int difficulty, GameMode gamemode, int fieldsize) {
		prepare(difficulty, gamemode, fieldsize);
		setTitle(R.string.custom_game_title);
	}

	private void setDifficultyLabel() {
		final String[] labels = getContext().getResources().getStringArray(R.array.difficulties);
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

	private int getDifficulty() {
		return DIFFICULTY_VALUES[difficulty.getProgress()];
	}

	private GameMode getGameMode() {
		return GameMode.from((int) gameMode.getSelectedItemPosition());
	}

	private int getFieldSize() {
		return GameConfig.FIELD_SIZES[fieldSize.getSelectedItemPosition()];
	}

	private boolean[] getPlayers() {
		boolean[] p = new boolean[4];
		p[0] = player1.isChecked();
		p[1] = player2.isChecked();
		p[2] = player3.isChecked();
		p[3] = player4.isChecked();
		if (GameMode.from(gameMode.getSelectedItemPosition()) == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
			/* this would otherwise request players two times, the server would hand out 2x2 = 4 players */
			p[2] = p[3] = false;
		}
		return p;
	}

	private int[] getStones() {
		final int[] result = new int[Shape.COUNT];
		for (int i = 0; i < Shape.COUNT; i++)
			result[i] = picker[Shape.get(i).getPoints() - 1].getValue();
		return result;
	}

	private GameConfig getConfiguration() {
		return new GameConfig(
			null,
			getGameMode(),
			false,
			getPlayers(),
			getDifficulty(),
			getStones(),
			getFieldSize()
		);
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
}
