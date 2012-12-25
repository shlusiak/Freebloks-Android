package de.saschahlusiak.freebloks.game;

import java.io.FileOutputStream;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.ServerListener;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.controller.SpielServer;
import de.saschahlusiak.freebloks.controller.Spielleiter;
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
import de.saschahlusiak.freebloks.view.opengl.Freebloks3DView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class FreebloksActivity extends Activity implements ActivityInterface, SpielClientInterface {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_GAME_FINISH = 4;
	
	public static final String GAME_STATE_FILE = "gamestate.bin";

	Freebloks3DView view;
	SpielClient spiel = null;
	Stone currentStone = null;
	SpielClientThread spielthread = null;
	ServerListener listener = null;
	Vibrator vibrator;
	boolean vibrate;
	NET_SERVER_STATUS lastStatus;
	
	static class RetainedConfig {
		SpielClientThread clientThread;
		NET_SERVER_STATUS lastStatus;
	}
	
	class ConnectTask extends AsyncTask<String,Void,String> {
		ProgressDialog progress;
		SpielClient mySpiel = null;
		boolean auto_start, request_player, show_lobby;
		
		ConnectTask(Spielleiter spiel, boolean request_player, boolean auto_start, boolean show_lobby) {
			mySpiel = new SpielClient(spiel);
			spielthread = new SpielClientThread(mySpiel);
			this.auto_start = auto_start;
			this.request_player = request_player;
			this.show_lobby = show_lobby;
		}
		
		@Override
		protected void onPreExecute() {
			view.setSpiel(null, null);
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
			if (request_player)
				mySpiel.request_player();
			if (auto_start)
				mySpiel.request_start();
			view.setSpiel(mySpiel, mySpiel.spiel);
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
				FreebloksActivity.this.finish();
			} else {
				if (! auto_start && show_lobby)
					showDialog(DIALOG_LOBBY);
				
				spiel.addClientInterface(FreebloksActivity.this);
				spielthread.start();
			}
			super.onPostExecute(result);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main_3d);

		view = (Freebloks3DView)findViewById(R.id.board);
		view.setActivity(this);
		
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		RetainedConfig config = (RetainedConfig)getLastNonConfigurationInstance();
		if (config != null) {
			spielthread = config.clientThread;
			lastStatus = config.lastStatus;
		}
		
		if (spielthread != null) {
			/* we just rotated and got *hot* objects */
			spiel = spielthread.spiel;
			spiel.addClientInterface(this);
			view.setSpiel(spiel, spiel.spiel);
		} else {
			if (savedInstanceState == null) {
				Bundle b;
				b = getIntent().getBundleExtra("gamestate");
				if (b != null) {
					if (readStateFromBundle(b)) {
						
					} else {
						Toast.makeText(this, "Could not restore game ", Toast.LENGTH_LONG).show();
						startNewGame();
					}
				} else /* without a game state, start a new game */
					startNewGame();				
			} else /* TODO: we should resume from previously saved data; don't just start a new game */
				startNewGame();
		}
	}

	@Override
	protected void onDestroy() {
		if (spielthread != null) try {
			spielthread.spiel.disconnect();
			spielthread.goDown();
			spielthread.join();
			spielthread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		spiel.removeClientInterface(this);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		view.onPause();
		if (spiel.spiel.current_player() >= 0)
			saveGameState(GAME_STATE_FILE);
		else
			deleteFile(FreebloksActivity.GAME_STATE_FILE);
		super.onPause();
	}

	@Override
	protected void onResume() {
		view.onResume();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		vibrate = prefs.getBoolean("vibrate", true);
		view.model.showSeeds = prefs.getBoolean("show_seeds", true);
		/* TODO: update wheel when changing show_opponents preference */
		view.model.showOpponents = prefs.getBoolean("show_opponents", true);
		view.model.showAnimations = prefs.getBoolean("show_animations", true);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		RetainedConfig config = new RetainedConfig();
		config.clientThread = spielthread;
		config.lastStatus = lastStatus;
		spielthread = null;
		return config;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		writeStateToBundle(outState);
	}
	
	private void writeStateToBundle(Bundle outState) {
		Log.d(tag, "onSaveInstanceState");
		Spielleiter l = spiel.spiel;
		outState.putSerializable("game", l);
	}
	
	private boolean readStateFromBundle(Bundle in) {
		try {
			Spielleiter spiel1 = (Spielleiter)in.getSerializable("game");
			Spielleiter spiel2 = (Spielleiter)spiel1.clone();
			
			SpielServer server = new SpielServer(spiel1, Ki.HARD);
			listener = new ServerListener(server, null, Network.DEFAULT_PORT, Ki.HARD);
			listener.start();
			
			/* this will start a new SpielClient, which needs to be restored 
			 * from saved gamestate first */
			new ConnectTask(spiel2, false, false, false).execute((String)null);

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void startNewGame() {
		String server = getIntent().getStringExtra("server");
		boolean request_player = getIntent().getBooleanExtra("request_player", true);
		
		if (server == null) {
			listener = new ServerListener(null, Network.DEFAULT_PORT, Ki.HARD);
			listener.start();
		}
		
		if (spielthread != null)
			spielthread.spiel.disconnect();
		
		new ConnectTask(null, request_player, (server == null), (server != null)).execute(server);
	}
	
	private void saveGameState(String filename) {
		FileOutputStream fos;
		try {
			fos = openFileOutput(filename, Context.MODE_PRIVATE);
			Parcel p = Parcel.obtain();
			Bundle b = new Bundle();
			writeStateToBundle(b);
			p.writeBundle(b);
			fos.write(p.marshall());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_optionsmenu, menu);
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {		
		case DIALOG_LOBBY:
			return new LobbyDialog(this, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					spiel.disconnect();
				}
			});
			
		case DIALOG_QUIT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to quit the current game? The game cannot be resumed (yet).");
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			return builder.create();
		
		case DIALOG_GAME_FINISH:
			return new GameFinishDialog(this);
			
		default:
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_LOBBY:
			((LobbyDialog)dialog).setSpiel(spiel);
			break;
		case DIALOG_GAME_FINISH:
			((GameFinishDialog)dialog).setData(spiel);
			((GameFinishDialog)dialog).setOnNewGameListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					startNewGame();
				}
			});
			break;
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.preferences:
			intent = new Intent(this, FreebloksPreferences.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void newCurrentPlayer(int player) {
//		Log.d(tag, "newCurrentPlayer(" + player + ")");
		selectCurrentStone(null);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.progressBar).setVisibility((spiel.spiel.is_local_player() || spiel.spiel.current_player() < 0) ? View.GONE : View.VISIBLE);
			}
		});
	}

	@Override
	public void stoneWasSet(NET_SET_STONE s) {
//		Log.d(tag, "stoneWasSet(...)");
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		
	}

	@Override
	public void gameFinished() {
		int i;
		Log.i(tag, "-- Game finished! --");
		for (i = 0; i < Spiel.PLAYER_MAX; i++) {
			Player player = spiel.spiel.get_player(i);
			Log.i(tag, (spiel.spiel.is_local_player(i) ? "*" : " ") + "Player " + i
					+ " has " + player.m_stone_count + " stones left and "
					+ -player.m_stone_points_left + " points.");
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showDialog(DIALOG_GAME_FINISH);
			}
		});
	}

	@Override
	public void chatReceived(final NET_CHAT c) {
		Log.d(tag, "chatReceived");
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

	@Override
	public void gameStarted() {
		if (listener != null) {
			listener.go_down();
			listener = null;
		}
			
		Log.d(tag, "Game started");
		for (int i = 0; i < Spiel.PLAYER_MAX; i++)
			if (spiel.spiel.is_local_player(i))
				Log.d(tag, "Local player: " + i);
	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		
	}

	@Override
	public void serverStatus(NET_SERVER_STATUS status) {
		Log.d(tag, "serverStatus()");
		lastStatus = status;
	}

	@Override
	public void onConnected(Spiel spiel) {
		Log.w(tag, "onConnected()");
	}

	@Override
	public void onDisconnected(Spiel spiel) {
		Log.w(tag, "onDisconnected()");
	}

	@Override
	public void selectCurrentStone(final Stone stone) {
		currentStone = stone;
	}

	@Override
	public boolean commitCurrentStone(Stone stone, int x, int y) {
		Log.w(tag, "commitCurrentStone(" + x + ", " + y + ")");
		if (!spiel.spiel.is_local_player())
			return false;
		if (spiel.spiel.is_valid_turn(stone, spiel.spiel.current_player(), 19 - y, x) != Stone.FIELD_ALLOWED)
			return false;
		
		spiel.set_stone(stone, 19 - y, x);
		selectCurrentStone(null);
		if (vibrate)
			vibrator.vibrate(100);
		return true;
	}
	
	public void vibrate(int ms) {
		if (vibrate)
			vibrator.vibrate(ms);
	}
	
	@Override
	public void onBackPressed() {
		if (spiel != null && spiel.spiel.current_player() >= 0 && lastStatus.clients > 1)
			showDialog(DIALOG_QUIT);
		else
			super.onBackPressed();
	}
}