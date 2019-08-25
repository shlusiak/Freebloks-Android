package de.saschahlusiak.freebloks.game;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.saschahlusiak.freebloks.R;

public class RulesActivity extends Activity implements View.OnClickListener {
	private static final String YOUTUBE_LINK = "https://www.youtube.com/watch?v=pc8nmWpcQWs";

	private FirebaseAnalytics analytics;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rules_activity);

		analytics = FirebaseAnalytics.getInstance(this);

		if (getActionBar() != null)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.youtube).setOnClickListener(this);

		analytics.logEvent("show_rules", null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_LINK));
		analytics.logEvent("show_rules_video", null);

		startActivity(intent);
	}
}
