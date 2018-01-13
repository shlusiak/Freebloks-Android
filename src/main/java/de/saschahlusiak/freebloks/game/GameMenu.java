package de.saschahlusiak.freebloks.game;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import de.saschahlusiak.freebloks.AboutActivity;
import de.saschahlusiak.freebloks.Global;
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
import de.saschahlusiak.freebloks.donate.DonateActivity;

public class GameMenu extends Dialog {
	ImageButton soundButton;
	FreebloksActivity activity;
	boolean soundon;

	private final boolean appIconIsDonate;
	private final ImageView appIcon;

	public GameMenu(Context context) {
		super(context);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game_menu_dialog);

		final long starts = prefs.getLong("rate_number_of_starts", 0);
		appIconIsDonate = (!Global.IS_VIP) && (starts % Global.DONATE_STARTS) == 0;

		appIcon = findViewById(R.id.appIcon);

		if (appIconIsDonate) {
			appIcon.setImageResource(R.drawable.ic_action_favorite);
		}
		appIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				intent = new Intent(getContext(), appIconIsDonate ? DonateActivity.class : AboutActivity.class);
				getContext().startActivity(intent);
			}
		});
		soundButton = findViewById(R.id.sound_toggle_button);
		soundButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				soundon = !soundon;
				activity = (FreebloksActivity)getOwnerActivity();
				soundButton.setImageResource(soundon ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp);
				activity.view.model.soundPool.setEnabled(soundon);
				activity.updateSoundMenuEntry();
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				Editor editor = prefs.edit();
				editor.putBoolean("sounds", soundon);
				editor.apply();

				Toast.makeText(getContext(), getContext().getString(soundon ? R.string.sound_on : R.string.sound_off), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (appIconIsDonate) {
			appIcon.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.heart));
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		activity = (FreebloksActivity)getOwnerActivity();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		soundon = prefs.getBoolean("sounds", true);
		soundButton.setImageResource(soundon ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp);
	}

	@Override
	public void onBackPressed() {
		getOwnerActivity().finish();
	}
}
