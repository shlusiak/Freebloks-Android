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
	
	String[] labels;
	String[] values;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics_activity);
		
		labels = getResources().getStringArray(R.array.statistics_labels);
		values = new String[labels.length];
		
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

	void refreshData() {
		int games = db.getTotalNumberOfGames(game_mode);
		int points = db.getTotalNumberOfPoints(game_mode);
		int perfect = db.getNumberOfPerfectGames(game_mode);
		int good = db.getNumberOfGoodGames(game_mode);
		int stones_left = db.getTotalNumberOfStonesLeft(game_mode);
		int stones_used = games * Stone.STONE_COUNT_ALL_SHAPES - stones_left;
		int i;

		for (i = 0; i < values.length; i++)
			values[i] = "";
		
		values[0] = String.format("%d", games);
		values[8] = String.format("%d", points);
		
		if (games == 0) /* avoid divide by zero */ {
			games = 1;
			stones_used = 0;
		}
		
		good -= perfect;
		values[1] = String.format("%.1f%%", 100.0f * (float)good / (float)games);
		values[2] = String.format("%.1f%%", 100.0f * (float)perfect / (float)games);
		for (i = 0; i < 4; i++) {
			int n = db.getNumberOfPlace(game_mode, i + 1);
			values[3 + i] = String.format("%.1f%%", 100.0f * (float)n / (float)games);
		}
		values[7] = String.format("%.1f%%", 100.0f * (float)stones_used / (float)games / (float)Stone.STONE_COUNT_ALL_SHAPES);

		adapter.notifyDataSetChanged();
	}
	
}
