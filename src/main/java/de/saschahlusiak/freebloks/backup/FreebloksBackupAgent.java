package de.saschahlusiak.freebloks.backup;

import de.saschahlusiak.freebloks.database.FreebloksDBOpenHandler;
import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.FROYO)
public class FreebloksBackupAgent extends BackupAgentHelper{
	static final String PREFS_BACKUP_KEY = "prefs";
	static final String DB_BACKUP_KEY = "database";

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, this.getPackageName() + "_preferences");
        addHelper(PREFS_BACKUP_KEY, helper);
        addHelper(DB_BACKUP_KEY, new FileBackupHelper(this, "../databases/" + FreebloksDBOpenHandler.DATABASE_NAME));
	}
}
