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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
		soundButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				activity = (FreebloksActivity)getOwnerActivity();
				soundButton.setCompoundDrawablesWithIntrinsicBounds(
						isChecked ? android.R.drawable.ic_lock_silent_mode_off : android.R.drawable.ic_lock_silent_mode,
						0,
						0,
						0);
				activity.view.model.soundPool.setEnabled(isChecked);
				activity.updateSoundMenuEntry();
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				Editor editor = prefs.edit();
				editor.putBoolean("sounds", isChecked);
				editor.commit();

				Toast.makeText(getContext(), getContext().getString(isChecked ? R.string.sound_on : R.string.sound_off), Toast.LENGTH_SHORT).show();
			}
		});
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.getBoolean("rate_show_again", true))
			findViewById(R.id.star).setVisibility(View.GONE);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		activity = (FreebloksActivity)getOwnerActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		soundButton.setChecked(prefs.getBoolean("sounds", true));
	}

	@Override
	public void onBackPressed() {
		getOwnerActivity().finish();
	}
}
