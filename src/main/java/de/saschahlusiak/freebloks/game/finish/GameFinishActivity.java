package de.saschahlusiak.freebloks.game.finish;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.game.GooglePlayGamesHelper;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.PlayerScore;
import de.saschahlusiak.freebloks.statistics.StatisticsActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

public class GameFinishActivity extends FragmentActivity implements View.OnClickListener {
	public static final int RESULT_NEW_GAME = RESULT_FIRST_USER + 1;
	public static final int RESULT_SHOW_MENU = RESULT_FIRST_USER + 2;

	private static final int REQUEST_LEADERBOARD = 1;
	private static final int REQUEST_ACHIEVEMENTS = 2;

	private GameFinishActivityViewModel viewModel;
	private GooglePlayGamesHelper gameHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.game_finish_activity);

		viewModel = new ViewModelProvider(this).get(GameFinishActivityViewModel.class);
		gameHelper = viewModel.getGameHelper();

		if (!viewModel.isInitialised()) {
			viewModel.setDataFromIntent(getIntent());
		}

		updateViews(viewModel.getData(), viewModel.getGameMode());

		findViewById(R.id.new_game).setOnClickListener(this);
		findViewById(R.id.show_main_menu).setOnClickListener(this);
		findViewById(R.id.statistics).setOnClickListener(this);
		findViewById(R.id.achievements).setOnClickListener(this);
		findViewById(R.id.leaderboard).setOnClickListener(this);

		viewModel.googleAccount.observe(this, signedIn -> {
			viewModel.getGameHelper().setWindowForPopups(getWindow());
			findViewById(R.id.achievements).setVisibility(signedIn ? View.VISIBLE : View.GONE);
			findViewById(R.id.leaderboard).setVisibility(signedIn ? View.VISIBLE : View.GONE);
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.new_game:
				setResult(RESULT_NEW_GAME);
				finish();
				break;
			case R.id.show_main_menu:
				setResult(RESULT_SHOW_MENU);
				finish();
				break;
			case R.id.statistics:
				Intent intent = new Intent(GameFinishActivity.this, StatisticsActivity.class);
				startActivity(intent);
				break;
			case R.id.achievements:
				gameHelper.startAchievementsIntent(this, REQUEST_ACHIEVEMENTS);
				break;
			case R.id.leaderboard:
				gameHelper.startLeaderboardIntent(this, getString(R.string.leaderboard_points_total), REQUEST_LEADERBOARD);
				break;
		}
	}

	private void updateViews(@NonNull PlayerScore[] data, @NonNull GameMode gameMode) {
		final ViewGroup[] t = new ViewGroup[4];
		final TextView place = findViewById(R.id.place);

		int i;

		t[0] = findViewById(R.id.place1);
		t[1] = findViewById(R.id.place2);
		t[2] = findViewById(R.id.place3);
		t[3] = findViewById(R.id.place4);

		if (gameMode == GameMode.GAMEMODE_2_COLORS_2_PLAYERS ||
			gameMode == GameMode.GAMEMODE_DUO ||
			gameMode == GameMode.GAMEMODE_JUNIOR ||
			gameMode == GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
			t[2].setVisibility(View.GONE);
			t[3].setVisibility(View.GONE);
		} else {
			t[2].setVisibility(View.VISIBLE);
			t[3].setVisibility(View.VISIBLE);
		}

		place.setText(R.string.game_finished);

		for (i = data.length - 1; i >= 0; i--) {
			final String name = data[i].getClientName();

			String s;
			((TextView)t[i].findViewById(R.id.name)).setText(name);
			t[i].findViewById(R.id.name).clearAnimation();

			((TextView)t[i].findViewById(R.id.place)).setText(String.format("%d.", data[i].getPlace()));

			s = getResources().getQuantityString(R.plurals.number_of_points, data[i].getTotalPoints(), data[i].getTotalPoints());
			((TextView)t[i].findViewById(R.id.points)).setText(s);
			s = "";
			if (data[i].getBonus() > 0)
				s += " (+" + data[i].getBonus()+ ")";

			((TextView)t[i].findViewById(R.id.bonus_points)).setText(s);

			((TextView)t[i].findViewById(R.id.stones)).setText(
					getResources().getQuantityString(R.plurals.number_of_stones_left, data[i].getStonesLeft(), data[i].getStonesLeft()));

			t[i].findViewById(R.id.data).setBackgroundDrawable(getScoreDrawable(data[i], gameMode));

			AnimationSet set = new AnimationSet(false);
			Animation a = new AlphaAnimation(0.0f, 1.0f);
			a.setStartOffset(i * 100);
			a.setDuration(600);
			a.setFillBefore(true);
			set.addAnimation(a);
			a = new TranslateAnimation(
					TranslateAnimation.RELATIVE_TO_SELF,
					1,
					TranslateAnimation.RELATIVE_TO_SELF,
					0,
					TranslateAnimation.RELATIVE_TO_SELF,
					0,
					TranslateAnimation.RELATIVE_TO_SELF,
					0);
			a.setStartOffset(200 + i * 100);
			a.setDuration(600);
			a.setFillBefore(true);
			set.addAnimation(a);

			if (data[i].isLocal()) {
				a = new TranslateAnimation(
						TranslateAnimation.RELATIVE_TO_SELF,
						0,
						TranslateAnimation.RELATIVE_TO_SELF,
						0.4f,
						TranslateAnimation.RELATIVE_TO_SELF,
						0,
						TranslateAnimation.RELATIVE_TO_SELF,
						0);
				a.setDuration(300);
				a.setInterpolator(new DecelerateInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);

				((TextView)t[i].findViewById(R.id.name)).setTextColor(Color.WHITE);
				((TextView)t[i].findViewById(R.id.place)).setTextColor(Color.WHITE);
				((TextView)t[i].findViewById(R.id.name)).setTypeface(Typeface.DEFAULT_BOLD);
				((TextView)t[i].findViewById(R.id.stones)).setTextColor(Color.WHITE);

				t[i].findViewById(R.id.name).startAnimation(a);

				a = new AlphaAnimation(0.5f, 1.0f);
				a.setDuration(750);
				a.setInterpolator(new LinearInterpolator());
				a.setRepeatMode(Animation.REVERSE);
				a.setRepeatCount(Animation.INFINITE);

				set.addAnimation(a);

				place.setText(getResources().getStringArray(R.array.places)[data[i].getPlace() - 1]);
			}
			t[i].findViewById(R.id.data).startAnimation(set);
		}
	}

	private Drawable getScoreDrawable(PlayerScore data, GameMode gameMode) {
		int color = Global.getPlayerColor(data.getColor1(), gameMode);
		LayerDrawable l;

		if (data.getColor2() >= 0)
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_2).mutate();
		else
			l = (LayerDrawable)getResources().getDrawable(R.drawable.bg_card_1).mutate();

		((GradientDrawable)l.findDrawableByLayerId(R.id.color1)).setColor(getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[color]));
		if (data.getColor2() >= 0) {
			color = Global.getPlayerColor(data.getColor2(), gameMode);
			((GradientDrawable)l.findDrawableByLayerId(R.id.color2)).setColor(getResources().getColor(Global.PLAYER_BACKGROUND_COLOR_RESOURCE[color]));
		}

		return l;
	}
}
