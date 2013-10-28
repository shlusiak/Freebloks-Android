package de.saschahlusiak.freebloks.stats;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class StatisticsActivity extends Activity {
	HighscoreDB db;
	StatisticsAdapter adapter;
	int game_mode = -1;
	
	final String[] labels = {
			"Games",
			"Points total",
			"Perfect",
			"1st place",
			"2nd place",
			"3rd place",
			"4th place"
		};
	final String[] values = new String[labels.length];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics_activity);
		
		db = new HighscoreDB(this);
		db.open();
		
		
		adapter = new StatisticsAdapter(this, labels, values);
		refreshData();
		((ListView) findViewById(R.id.listView)).setAdapter(adapter);
		
		findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		db.close();
		db = null;
		super.onDestroy();
	}

	void refreshData() {
		int games = db.getTotalNumberOfGames(game_mode);
		int points = db.getTotalNumberOfPoints(game_mode);
		int perfect = db.getNumberOfPerfectGames(game_mode);

		adapter.values[0] = String.format("%d", games);
		adapter.values[1] = String.format("%d", points);
		if (games > 0) {
			adapter.values[2] = String.format("%.0f%% / %d", 100.0f * (float)perfect / (float)games, perfect);
			for (int i = 0; i < 4; i++) {
				int n = db.getNumberOfPlace(game_mode, i + 1);
				adapter.values[3 + i] = String.format("%.0f%% / %d", 100.0f * (float)n / (float)games, n);
			}
		}

		adapter.notifyDataSetChanged();
	}
	
}
