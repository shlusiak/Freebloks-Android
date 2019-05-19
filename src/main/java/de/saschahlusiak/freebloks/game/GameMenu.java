package de.saschahlusiak.freebloks.game;

import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.google.firebase.analytics.FirebaseAnalytics;
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
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;

public class GameMenu extends Dialog implements View.OnClickListener, View.OnLongClickListener {
	private ImageButton soundButton;
	private FreebloksActivity activity;
	private boolean soundon;

	private final boolean appIconIsDonate;
	private final ImageView appIcon;

	public GameMenu(Context context) {
		super(context);

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game_menu_dialog);

		findViewById(R.id.new_game).setOnClickListener(this);
		findViewById(R.id.new_game).setOnLongClickListener(this);
		findViewById(R.id.resume_game).setOnClickListener(this);
		findViewById(R.id.preferences).setOnClickListener(this);
		findViewById(R.id.join_game).setOnClickListener(this);
		findViewById(R.id.rules).setOnClickListener(this);
		findViewById(R.id.new_game_custom).setOnClickListener(this);

		final long starts = prefs.getLong("rate_number_of_starts", 0);
		appIconIsDonate = (!Global.IS_VIP) && (starts % Global.DONATE_STARTS) == 0;

		appIcon = findViewById(R.id.appIcon);

		if (appIconIsDonate) {
			FirebaseAnalytics.getInstance(context).logEvent("show_donate_button", null);

			appIcon.setImageResource(R.drawable.ic_action_favorite);
		}
		appIcon.setOnClickListener(this);
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

	public void setResumeEnabled(boolean canresume) {
		findViewById(R.id.resume_game).setEnabled(canresume);
		setCanceledOnTouchOutside(canresume);
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

	@Override
	public void onClick(View v) {
		Intent intent;
		activity = (FreebloksActivity)getOwnerActivity();
		switch (v.getId())
		{
			case R.id.new_game:
				dismiss();
				activity.showDialog(FreebloksActivity.DIALOG_SINGLE_PLAYER);
				break;

			case R.id.resume_game:
				dismiss();
				break;

			case R.id.preferences:
				intent = new Intent(getContext(), FreebloksPreferences.class);
				getContext().startActivity(intent);
				break;

			case R.id.appIcon:
				intent = new Intent(getContext(), appIconIsDonate ? DonateActivity.class : AboutActivity.class);
				getContext().startActivity(intent);
				break;

			case R.id.join_game:
				activity.showDialog(FreebloksActivity.DIALOG_JOIN);
				break;

			case R.id.new_game_custom:
				activity.showDialog(FreebloksActivity.DIALOG_CUSTOM_GAME);
				break;

			case R.id.rules:
				intent = new Intent(getContext(), RulesActivity.class);
				getContext().startActivity(intent);
				break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		activity = (FreebloksActivity)getOwnerActivity();
		switch (v.getId())
		{
			case R.id.new_game:
				activity.startNewGame();
				dismiss();
				return true;

			default:
				return false;
		}
	}
}
