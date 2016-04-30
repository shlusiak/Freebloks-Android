package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GameMenu extends Dialog {
	ToggleButton soundButton;
	FreebloksActivity activity;

	public GameMenu(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game_menu_dialog);

		findViewById(R.id.imageView1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), AboutActivity.class);
				getContext().startActivity(intent);
			}
		});
		soundButton = (ToggleButton)findViewById(R.id.sound_toggle_button);
		soundButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isChecked = soundButton.isChecked();
				activity = (FreebloksActivity)getOwnerActivity();
				soundButton.setCompoundDrawablesWithIntrinsicBounds(
						isChecked ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp,
								0,
								0,
								0);
				activity.view.model.soundPool.setEnabled(isChecked);
				activity.updateSoundMenuEntry();
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				Editor editor = prefs.edit();
				editor.putBoolean("sounds", isChecked);
				editor.apply();

				Toast.makeText(getContext(), getContext().getString(isChecked ? R.string.sound_on : R.string.sound_off), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		activity = (FreebloksActivity)getOwnerActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		soundButton.setChecked(prefs.getBoolean("sounds", true));
		soundButton.setCompoundDrawablesWithIntrinsicBounds(
				prefs.getBoolean("sounds", true) ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp,
				0,
				0,
				0);
	}

	@Override
	public void onBackPressed() {
		getOwnerActivity().finish();
	}
}
