package de.saschahlusiak.freebloks.game.finish;

import android.app.backup.BackupManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import de.saschahlusiak.freebloks.database.HighscoreDB;

class AddScoreToDBTask extends AsyncTask<PlayerData,Void,Void> {
	int game_mode;
	Context context;
	
	AddScoreToDBTask(Context context, int game_mode) {
		this.game_mode = game_mode;
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(PlayerData... data) {
		HighscoreDB db = new HighscoreDB(context);
		if (db.open()) {
			for (int i = 0; i < data.length; i++) if (data[i].is_local) {
				int flags = 0;
				if (data[i].is_perfect)
					flags |= HighscoreDB.FLAG_IS_PERFECT;

				db.addHighscore(
						game_mode,
						data[i].points,
						data[i].stones_left,
						data[i].player1,
						data[i].place,
						flags);

			}
			db.close();
			
			if (Build.VERSION.SDK_INT >= 8) {
				BackupManager backupManager = new BackupManager(context);
				backupManager.dataChanged();
			}
		}
		return null;
	}
	
}