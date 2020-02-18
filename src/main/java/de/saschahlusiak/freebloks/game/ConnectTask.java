package de.saschahlusiak.freebloks.game;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;

import de.saschahlusiak.freebloks.client.GameClient;

class ConnectTask extends AsyncTask<String,Void,Exception> implements OnCancelListener {
	private static final String tag = ConnectTask.class.getSimpleName();

	private FreebloksActivity activity;
	private GameClient myclient;
	private boolean show_lobby;
	private Runnable connectedRunnable;

	ConnectTask(@NonNull GameClient client, boolean show_lobby, Runnable connectedRunnable) {
		this.myclient = client;
		this.show_lobby = show_lobby;
		this.connectedRunnable = connectedRunnable;
	}
	
	void setActivity(FreebloksActivity freebloksActivity) {
		this.activity = freebloksActivity;
	}

	@Override
	protected void onPreExecute() {
		activity.showDialog(FreebloksActivity.DIALOG_PROGRESS, null);
		super.onPreExecute();
	}

	@Override
	protected Exception doInBackground(String... params) {
		try {
			// hostname may be null for "localhost"
			final String hostname = params[0];
			final String name = hostname == null ? "(null)" : hostname;
			Crashlytics.log(Log.INFO, tag, "Connecting to: " + name);
			Crashlytics.setString("server", name);

			myclient.connect(activity, hostname, GameClient.DEFAULT_PORT);
		} catch (IOException e) {
			if (isCancelled())
				return null;
			return e;
		}
		Crashlytics.log(Log.INFO, tag, "connected");
		if (isCancelled()) {
			return null;
		}
		return null;
	}

	@Override
	protected void onCancelled() {
		Crashlytics.log(Log.INFO, tag, "cancelled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Exception result) {
		activity.connectTask = null;
		if (activity.view == null)
			return;

		activity.connectTask = null;
		activity.dismissDialog(FreebloksActivity.DIALOG_PROGRESS);

		if (result != null) {
			Crashlytics.logException(result);

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setMessage(result.getMessage());
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			activity.canresume = false;
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					ConnectTask.this.activity.showDialog(FreebloksActivity.DIALOG_GAME_MENU);
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					ConnectTask.this.activity.showDialog(FreebloksActivity.DIALOG_GAME_MENU);
				}
			});
			builder.create().show();
		} else {
			if (show_lobby)
				activity.showDialog(FreebloksActivity.DIALOG_LOBBY);

			if (connectedRunnable != null)
				connectedRunnable.run();
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		myclient.disconnect();
		cancel(true);
		activity.connectTask = null;
		activity.canresume = false;
		activity.showDialog(FreebloksActivity.DIALOG_GAME_MENU);
	}
}