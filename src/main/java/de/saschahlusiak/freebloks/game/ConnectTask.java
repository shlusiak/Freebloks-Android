package de.saschahlusiak.freebloks.game;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.crashlytics.android.Crashlytics;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.network.Network;

class ConnectTask extends AsyncTask<String,Void,Exception> implements OnCancelListener {
	private static final String tag = ConnectTask.class.getSimpleName();

	private FreebloksActivity activity;
	private SpielClient myclient;
	private boolean show_lobby;
	private Runnable connectedRunnable;

	ConnectTask(SpielClient client, boolean show_lobby, Runnable connectedRunnable) {
		this.myclient = client;
		this.show_lobby = show_lobby;
		this.connectedRunnable = connectedRunnable;
	}
	
	void setActivity(FreebloksActivity freebloksActivity) {
		this.activity = freebloksActivity;
	}

	@Override
	protected void onPreExecute() {
		activity.lastStatus = null;
		activity.view.setSpiel(null, null);
		activity.chatButton.setVisibility(View.INVISIBLE);
		activity.chatEntries.clear();
		activity.showDialog(FreebloksActivity.DIALOG_PROGRESS, null);
		super.onPreExecute();
	}

	@Override
	protected Exception doInBackground(String... params) {
		try {
			final String name = params[0] == null ? "(null)" : params[0];
			Crashlytics.log(Log.INFO, tag, "Connecting to: " + name);
			Crashlytics.setString("server", name);

			myclient.connect(activity, params[0], Network.DEFAULT_PORT);
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
		if (activity.client != null)
		{
			activity.client.disconnect();
			activity.client = null;
		}
		if (activity.view == null)
			return;

		activity.client = this.myclient;
		activity.connectTask = null;
		activity.view.setSpiel(myclient, myclient.spiel);
		activity.dismissDialog(FreebloksActivity.DIALOG_PROGRESS);
		if (result != null) {
			Crashlytics.logException(result);

			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setMessage(result.getMessage());
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			activity.canresume = false;
			if (activity.spielthread != null)
				activity.spielthread.goDown();
			activity.spielthread = null;
			activity.client.disconnect();
			activity.client = null;
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

			myclient.addClientInterface(activity);

			if (connectedRunnable != null)
				connectedRunnable.run();

			activity.newCurrentPlayer(myclient.spiel.current_player());
		}
		super.onPostExecute(result);
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