package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.donate.DonateActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class RateAppDialog extends Dialog {
	private static final String tag = RateAppDialog.class.getSimpleName();
	
	SharedPreferences prefs;

	public RateAppDialog(Context context) {
		super(context, R.style.Theme_Freebloks_Light_Dialog);
		prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rate_app_dialog);
		setTitle(R.string.rate_freebloks_title);
		
		findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=de.saschahlusiak.freebloks"));
				Editor editor = prefs.edit();
				editor.putBoolean("rate_show_again", false);
				editor.commit();
				dismiss();
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.later).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Editor editor = prefs.edit();
				editor.putBoolean("rate_show_again", false);
				editor.commit();
				dismiss();
			}
		});
		findViewById(R.id.link).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), DonateActivity.class);
				getContext().startActivity(intent);
			}
		});
	}
	
	public static boolean checkShowRateDialog(Activity context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getBaseContext());
		
		if (prefs.getBoolean("rate_show_again", true)) {
			long starts = prefs.getLong("rate_number_of_starts", 0);
			long first_started = prefs.getLong("rate_first_started", 0);
			boolean show = false;
			Editor editor = prefs.edit();

			if (first_started <= 0) {
				first_started = System.currentTimeMillis();
			}
			
			starts++;
			Log.d(tag, "started " + starts + " times");
			Log.d(tag, "elapsed time since first start: " + (System.currentTimeMillis() - first_started));
			
			if (starts >= Global.RATE_MIN_STARTS && (System.currentTimeMillis() - first_started >= Global.RATE_MIN_ELAPSED)) {
				starts = 0;
				first_started = System.currentTimeMillis();
				show = true;
			}
			
			editor.putLong("rate_first_started", first_started);
			editor.putLong("rate_number_of_starts", starts);
			editor.commit();
			return show;
		}
		return false;
	}
}
