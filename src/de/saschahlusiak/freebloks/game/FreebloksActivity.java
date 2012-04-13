package de.saschahlusiak.freebloks.game;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.R.id;
import de.saschahlusiak.freebloks.R.layout;
import de.saschahlusiak.freebloks.R.menu;
import de.saschahlusiak.freebloks.R.string;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
import de.saschahlusiak.freebloks.model.Ki;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.network.Network;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import de.saschahlusiak.freebloks.view.FreebloksViewInterface;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class FreebloksActivity extends Activity  {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_JOIN = 1;
	static final int DIALOG_LOBBY = 2;

	FreebloksViewInterface view;
	Gallery stoneGallery;
	StoneGalleryAdapter stoneGalleryAdapter;
	SpielClient spiel = null;

	class KIThread extends Thread implements SpielClientInterface {
		SpielClient spiel;
		boolean godown;
		Ki ki = new Ki();
		
		KIThread(SpielClient spiel) {
			this.spiel = spiel;
			spiel.addClientInterface(this);
		}
		
		public synchronized boolean getGoDown() {
			return godown;
		}
		
		public synchronized void goDown() {
			godown = true;
		}

		@Override
		public void run() {
			godown = false;
		
			spiel.request_player();
//			spiel.request_player();
//			spiel.request_player();
//			spiel.request_player();

			do {
				if (!spiel.poll(true))
					break;
				if (getGoDown()) {
					Log.i("KIThread", "detaching");
					spiel.removeClientInterface(this);
					return;
				}
			} while (spiel.isConnected());
			spiel.disconnect();
			spiel.removeClientInterface(this);
			Log.i("KIThread", "disconnected, thread going down");
		}
		
		public void gameStarted() {
			int i;
			Log.d(tag, "Game started");
			for (i = 0; i < Spiel.PLAYER_MAX; i++)
				if (spiel.is_local_player(i))
					Log.d(tag, "Local player: " + i);
			view.updateView();
		}

		public void newCurrentPlayer(int player) {
			updateStoneGallery(player);
			
			if (!spiel.is_local_player())
				return;

			/* Ermittle CTurn, den die KI jetzt setzen wuerde */
			view.updateView();
			Turn turn = ki.get_ki_turn(spiel, spiel.current_player(), 5);
			Stone stone;
			if (turn == null) {
				Log.e(tag, "Player " + player + ": Did not find a valid move");
				return;
			}
			stone = spiel.get_current_player().get_stone(turn.m_stone_number);
			stone.mirror_rotate_to(turn.m_mirror_count, turn.m_rotate_count);
			spiel.set_stone(stone, turn.m_stone_number, turn.m_y, turn.m_x);
		}
		
		void updateStoneGallery(int player) {
			final Player p = (player < 0) ? null : spiel.get_player(player);
			stoneGallery.post(new Runnable() {
				@Override
				public void run() {
					if (p != null)
						stoneGalleryAdapter.setPlayer(p);
					stoneGalleryAdapter.notifyDataSetChanged();
				}
			});
		}
		
		void updateStoneGallery() {
			stoneGallery.post(new Runnable() {
				@Override
				public void run() {
					stoneGalleryAdapter.notifyDataSetChanged();
				}
			});
		}

		public void chatReceived(final NET_CHAT c) {
			if (spiel.current_player() < 0)
				return;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (c.client == -1)
						Toast.makeText(FreebloksActivity.this, "* " + c.text,
								Toast.LENGTH_LONG).show();
					else
						Toast.makeText(FreebloksActivity.this,
								"Client " + c.client + ": " + c.text,
								Toast.LENGTH_LONG).show();
				}
			});
		}

		public void gameFinished() {
			int i;
			Log.i(tag, "-- Game finished! --");
			for (i = 0; i < Spiel.PLAYER_MAX; i++) {
				Player player = spiel.get_player(i);
				Log.i(tag, (spiel.is_local_player(i) ? "*" : " ") + "Player " + i
						+ " has " + player.m_stone_count + " stones left and "
						+ -player.m_stone_points_left + " points.");
			}
			updateStoneGallery();

			spiel.disconnect();
			view.updateView();
		}

		@Override
		public void stoneWasSet(NET_SET_STONE s) {
			view.updateView();
			updateStoneGallery();
		}

		@Override
		public void hintReceived(NET_SET_STONE s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void stoneUndone(Stone s, Turn t) {
			view.updateView();
			updateStoneGallery();
		}

		@Override
		public void serverStatus(NET_SERVER_STATUS status) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class ConnectTask extends AsyncTask<String,Void,String> {
		ProgressDialog progress;
		SpielClient mySpiel = null;
		
		@Override
		protected void onPreExecute() {
			view.setSpiel(null);
			mySpiel = new SpielClient();
			kithread = new KIThread(mySpiel);
			progress = new ProgressDialog(FreebloksActivity.this);
			progress.setMessage("Connecting...");
			progress.setIndeterminate(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(true);
			progress.show();
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			try {
				mySpiel.connect(params[0], Network.DEFAULT_PORT);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
			view.setSpiel(mySpiel);
			return null;
		}
		
		@Override
		protected void onCancelled() {
			progress.dismiss();
			super.onCancelled();
		}
		
		@Override
		protected void onPostExecute(String result) {
			spiel = mySpiel;
			progress.dismiss();
			if (result != null) {
				Toast.makeText(FreebloksActivity.this, result, Toast.LENGTH_LONG).show();
			} else {
				showDialog(DIALOG_LOBBY);
			}
			kithread.start();
			super.onPostExecute(result);
		}
		

	}
	
	KIThread kithread = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		setContentView(prefs.getBoolean("view_opengl", true) ? R.layout.main_3d : R.layout.main);

		view = (FreebloksViewInterface)findViewById(R.id.board);
		stoneGallery = (Gallery)findViewById(R.id.stoneGallery);
		stoneGalleryAdapter = new StoneGalleryAdapter(this, null);
		stoneGallery.setAdapter(stoneGalleryAdapter);
		spiel = (SpielClient)getLastNonConfigurationInstance();
		view.setSpiel(spiel);
		if (spiel != null) {
			kithread = new KIThread(spiel);
			kithread.start();
		} else 
			showDialog(DIALOG_JOIN);
	}

	@Override
	protected void onDestroy() {
		if (kithread != null) try {
			kithread.spiel.disconnect();
			kithread.goDown();
			kithread.join();
			kithread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (kithread != null) try {
			kithread.goDown();
			kithread.join();
			spiel.clearClientInterface();
			kithread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return spiel;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_JOIN:
			final Dialog addDialog = new Dialog(this);
			addDialog.setContentView(R.layout.join_game_dialog);
			addDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			addDialog.setTitle(R.string.menu_join_game);
			((EditText)addDialog.findViewById(R.id.server)).setText("192.168.2.159");
			Button okAdd = (Button) addDialog.findViewById(android.R.id.button1);
			okAdd.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String n = ((EditText)addDialog.findViewById(R.id.server)).getText().toString();
					if (kithread != null && kithread.spiel != null)
						kithread.spiel.disconnect();

					new ConnectTask().execute(n);
					addDialog.dismiss();
				}
			});
			((Button)addDialog.findViewById(android.R.id.button2)).setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					addDialog.dismiss();
//					if (view.getSpiel() == null)
//						finish();						
				}
			});
			return addDialog;
			
		case DIALOG_LOBBY:
			return new LobbyDialog(this, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					spiel.disconnect();
				}
			});
		default:
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_LOBBY:
			((LobbyDialog)dialog).setSpiel(spiel);
			break;
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	/** Called when a menu item is selected. */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.new_local_game:
			return true;
			
		case R.id.join_game:
			showDialog(DIALOG_JOIN);
			return true;

		case R.id.preferences:
			intent = new Intent(this, FreebloksPreferences.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}