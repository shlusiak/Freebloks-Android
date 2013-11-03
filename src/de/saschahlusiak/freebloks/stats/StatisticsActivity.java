package de.saschahlusiak.freebloks.stats;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.database.HighscoreDB;
import de.saschahlusiak.freebloks.model.Stone;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

public class StatisticsActivity extends Activity {
	HighscoreDB db;
	StatisticsAdapter adapter;
	int game_mode = -1;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics_activity);
		
		db = new HighscoreDB(this);
		db.open();
		
		
		adapter = new StatisticsAdapter(this, labels, values1);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.stats_optionsmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear:
			db.clearHighscores();
			refreshData();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	final String[] labels = {
			"Games played",
			"Good games (+15)",
			"Perfect games (+20)",
			"1st place",
			"2nd place",
			"3rd place",
			"4th place",
			"Stones used (avg)",
			"Points total",
		};
	final String[] values1 = new String[labels.length];

	void refreshData() {
		int games = db.getTotalNumberOfGames(game_mode);
		int points = db.getTotalNumberOfPoints(game_mode);
		int perfect = db.getNumberOfPerfectGames(game_mode);
		int good = db.getNumberOfGoodGames(game_mode);
		int stones_left = db.getTotalNumberOfStonesLeft(game_mode);
		int stones_used = games * Stone.STONE_COUNT_ALL_SHAPES - stones_left;
		int i;

		for (i = 0; i < values1.length; i++)
			values1[i] = "";
		
		adapter.values1[0] = String.format("%d", games);
		adapter.values1[8] = String.format("%d", points);
		
		if (games == 0) /* avoid divide by zero */ {
			games = 1;
			stones_used = 0;
		}
		
		good -= perfect;
		adapter.values1[1] = String.format("%.1f%%", 100.0f * (float)good / (float)games);
		adapter.values1[2] = String.format("%.1f%%", 100.0f * (float)perfect / (float)games);
//		adapter.values1[2] = String.format("%d", perfect);
		for (i = 0; i < 4; i++) {
			int n = db.getNumberOfPlace(game_mode, i + 1);
			adapter.values1[3 + i] = String.format("%.1f%%", 100.0f * (float)n / (float)games);
//			adapter.values1[3 + i] = String.format("%d", n);
		}
		adapter.values1[7] = String.format("%.1f%%", 100.0f * (float)stones_used / (float)games / (float)Stone.STONE_COUNT_ALL_SHAPES);
//		adapter.values1[7] = String.format("%.2f", (float)stones_used / (float)games);

		adapter.notifyDataSetChanged();
	}
	
}
