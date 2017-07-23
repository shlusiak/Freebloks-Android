package de.saschahlusiak.freebloks.game;

import java.io.IOException;
import java.lang.Thread.State;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.network.Network;

class ConnectTask extends AsyncTask<String,Void,String> implements OnCancelListener {
	private FreebloksActivity activity;
	SpielClient myclient = null;
	boolean show_lobby;
	Runnable connectedRunnable;

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
	protected String doInBackground(String... params) {
		try {
			Log.d("ConnectTask", "connecting to " + params[0]);
			myclient.connect(activity, params[0], Network.DEFAULT_PORT);
		} catch (IOException e) {
			if (isCancelled())
				return null;
			return e.getMessage();
		}
		Log.d("ConnectTask", "connected");
		if (isCancelled()) {
			return null;
		}
		if (connectedRunnable != null)
			connectedRunnable.run();
		return null;
	}

	@Override
	protected void onCancelled() {
		Log.d("ConnectTask", "onCancelled");
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		activity.connectTask = null;
		activity.client = this.myclient;
		activity.connectTask = null;
		activity.view.setSpiel(myclient, myclient.spiel);
		activity.dismissDialog(FreebloksActivity.DIALOG_PROGRESS);
		if (result != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setMessage(result);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
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
			activity.newCurrentPlayer(myclient.spiel.current_player());
			/* TODO: there is probably a race condition, when the device is
			 * rotated, the task finishes and a new task is started while
			 * the thread is running.
			 */
			if (activity.spielthread.getState() == State.NEW)
				activity.spielthread.start();
		}
		super.onPostExecute(result);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		myclient.disconnect();
		cancel(true);
		activity.connectTask = null;
		activity.showDialog(FreebloksActivity.DIALOG_GAME_MENU);
	}
}