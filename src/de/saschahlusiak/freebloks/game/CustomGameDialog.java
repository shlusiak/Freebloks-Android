package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import android.app.Dialog;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CustomGameDialog extends Dialog implements OnSeekBarChangeListener {
	SeekBar difficulty;
	TextView difficulty_label;

	final static int DIFFICULTY_MAX = 10; /* 0..10 = 11 */
	final static int DIFFICULTY_DEFAULT = 8; /* TODO: maybe save and restore default value */
	final static int DIFFICULTY_VALUES[] = {
		200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
	};

	public CustomGameDialog(Context context) {
		super(context);
		setContentView(R.layout.game_menu_new_custom_game);
		
		difficulty = (SeekBar)findViewById(R.id.difficulty);
		difficulty_label = (TextView)findViewById(R.id.difficulty_label);
		difficulty.setOnSeekBarChangeListener(this);
		difficulty.setMax(DIFFICULTY_MAX);
		difficulty.setProgress(DIFFICULTY_DEFAULT);
		setLabel();
		
		/* TODO: translate */
		setTitle("New game");
	}
	
	public void prepare() {
		int p = (int)(Math.random() * 4.0);
		((CheckBox)findViewById(R.id.player1)).setChecked(p == 0);
		((CheckBox)findViewById(R.id.player2)).setChecked(p == 1);
		((CheckBox)findViewById(R.id.player3)).setChecked(p == 2);
		((CheckBox)findViewById(R.id.player4)).setChecked(p == 3);
	}
	
	void setLabel() {
		/* TODO: translate */
		final String labels[] = { "nuts", "hard", "medium", "easy", "very easy" };
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
		/* TODO: localize */
		difficulty_label.setText(String.format("%s (%d)", labels[text], difficulty.getProgress()));
	}
	
	public int getDifficulty() {
		return DIFFICULTY_VALUES[difficulty.getProgress()];
	}
	
	public boolean[] getPlayers() {
		boolean p[] = new boolean[4];
		p[0] = ((CheckBox)findViewById(R.id.player1)).isChecked();
		p[1] = ((CheckBox)findViewById(R.id.player2)).isChecked();
		p[2] = ((CheckBox)findViewById(R.id.player3)).isChecked();
		p[3] = ((CheckBox)findViewById(R.id.player4)).isChecked();
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
