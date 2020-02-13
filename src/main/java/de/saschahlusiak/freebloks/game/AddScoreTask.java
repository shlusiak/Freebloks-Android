package de.saschahlusiak.freebloks.game;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.PlayerScore;
import de.saschahlusiak.freebloks.database.HighscoreDB;

public class AddScoreTask extends AsyncTask<PlayerScore,Void,Void> {
	private GameMode game_mode;
	private Context context;

	public AddScoreTask(Context context, GameMode game_mode) {
		this.game_mode = game_mode;
		this.context = context;
	}

	@Override
	protected Void doInBackground(PlayerScore... data) {
		HighscoreDB db = new HighscoreDB(context);
		if (data == null)
			return null;

		try {
			db.open();

			for (int i = 0; i < data.length; i++) if (data[i].isLocal()) {
				int flags = 0;
				if (data[i].isPerfect())
					flags |= HighscoreDB.FLAG_IS_PERFECT;

				db.addHighscore(
					game_mode,
					data[i].getPoints(),
					data[i].getStonesLeft(),
					data[i].getPlayer1(),
					data[i].getPlace(),
					flags);
			}

			db.close();
		}
		catch (SQLiteException e) {
			Crashlytics.logException(e);
			e.printStackTrace();
		}
		return null;
	}
}