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
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

public class FreebloksActivity extends Activity implements ActivityInterface, SpielClientInterface {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_GAME_FINISH = 4;
	
	public static final String GAME_STATE_FILE = "gamestate.bin";

	Freebloks3DView view;
	SpielClient client = null;
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
		SpielClient myclient = null;
		boolean show_lobby;
		Runnable connectedRunnable;
		
		ConnectTask(SpielClient client, boolean show_lobby, Runnable connectedRunnable) {
			this.myclient = client;
			spielthread = new SpielClientThread(myclient);
			this.show_lobby = show_lobby;
			this.connectedRunnable = connectedRunnable;
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
				this.myclient.connect(params[0], Network.DEFAULT_PORT);
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
			if (connectedRunnable != null)
				connectedRunnable.run();
			view.setSpiel(myclient, myclient.spiel);
			return null;
		}
		
		@Override
		protected void onCancelled() {
			progress.dismiss();
			super.onCancelled();
		}
		
		@Override
		protected void onPostExecute(String result) {
			FreebloksActivity.this.client = this.myclient;
			progress.dismiss();
			if (result != null) {
				Toast.makeText(FreebloksActivity.this, result, Toast.LENGTH_LONG).show();
				FreebloksActivity.this.finish();
			} else {
				if (show_lobby)
					showDialog(DIALOG_LOBBY);
				
				myclient.addClientInterface(FreebloksActivity.this);
				newCurrentPlayer(myclient.spiel.current_player());
				spielthread.start();
			}
			super.onPostExecute(result);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "onCreate");
		
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
		if (savedInstanceState != null) {
			view.setScale(savedInstanceState.getFloat("view_scale", 1.0f));
		} else {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			view.setScale(pref.getFloat("view_scale", 1.0f));
		}
		
		if (spielthread != null) {
			/* we just rotated and got *hot* objects */
			client = spielthread.client;
			client.addClientInterface(this);
			view.setSpiel(client, client.spiel);
			newCurrentPlayer(client.spiel.current_player());
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
		Log.d(tag, "onDestroy");
		if (spielthread != null) try {
			spielthread.client.disconnect();
			spielthread.goDown();
			spielthread.join();
			spielthread = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		client.removeClientInterface(this);
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		Log.d(tag, "onPause1");
		view.onPause();
		if (client.spiel.current_player() >= 0)
			saveGameState(GAME_STATE_FILE);
		else
			deleteFile(FreebloksActivity.GAME_STATE_FILE);
		Log.d(tag, "onPause2");
		super.onPause();
	}

	@Override
	protected void onResume() {
		view.onResume();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		editor.putFloat("view_scale", view.getScale());
		editor.commit();
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
		view.model.snapAid = prefs.getBoolean("snap_aid", true);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d(tag, "onRetainNonConfigurationInstance");
		RetainedConfig config = new RetainedConfig();
		config.clientThread = spielthread;
		config.lastStatus = lastStatus;
		spielthread = null;
		return config;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(tag, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putFloat("view_scale", view.getScale());
		writeStateToBundle(outState);
	}
	
	private void writeStateToBundle(Bundle outState) {
		Spielleiter l = client.spiel;
		outState.putSerializable("game", l);
	}
	
	private boolean readStateFromBundle(Bundle in) {
		try {
			Spielleiter spiel1 = (Spielleiter)in.getSerializable("game");
			Spielleiter spiel2 = (Spielleiter)spiel1.clone();
			
			final SpielServer server = new SpielServer(spiel1, Ki.HARD);
			listener = new ServerListener(server, null, Network.DEFAULT_PORT, Ki.HARD);
			listener.start();
			
			/* this will start a new SpielClient, which needs to be restored 
			 * from saved gamestate first */
			SpielClient client = new SpielClient(spiel2);
			ConnectTask task = new ConnectTask(client, false, new Runnable() {
				@Override
				public void run() {
					server.resume_game();
					if (listener != null)
						listener.go_down();
					listener = null;
				}
			});
			task.execute((String)null);

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void startNewGame() {
		final String server = getIntent().getStringExtra("server");
		final boolean request_player = getIntent().getBooleanExtra("request_player", true);
		
		if (server == null) {
			listener = new ServerListener(null, Network.DEFAULT_PORT, Ki.HARD);
			listener.start();
		}
		
		if (spielthread != null)
			spielthread.client.disconnect();
		
		Spielleiter spiel = new Spielleiter(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X);
		final SpielClient client = new SpielClient(spiel);
		spiel.start_new_game();
		spiel.set_stone_numbers(0, 0, 0, 0, 0);
		
		ConnectTask task = new ConnectTask(client, server != null, new Runnable() {
			@Override
			public void run() {
				if (request_player)
					client.request_player();
				if (server == null)
					client.request_start();
			}
		});
		task.execute(server);
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
					client.disconnect();
				}
			});
			
		case DIALOG_QUIT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to quit the current game? The game can be resumed, but all other players will be replaced by computers.");
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
			((LobbyDialog)dialog).setSpiel(client);
			break;
		case DIALOG_GAME_FINISH:
			((GameFinishDialog)dialog).setData(client);
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
		final int current = player;
		final boolean local = client.spiel.is_local_player();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				/* TODO: generalize */
				final int colors[] = {
						Color.argb(204, 0, 0, 96),
						Color.argb(204, 96, 96, 0),
						Color.argb(204, 96, 0, 0),
						Color.argb(204, 0, 96, 0),
				};
				/* TODO: generalize */
				final String names[] = {
						"Blue",
						"Yellow",
						"Red",
						"Green"
				};

				View v;
				v = findViewById(R.id.progressBar);
				v.setVisibility((local || current < 0) ? View.GONE : View.VISIBLE);
				v = findViewById(R.id.currentPlayerLayout);
				v.setVisibility(View.VISIBLE);
				v.clearAnimation();
				TextView t = (TextView)findViewById(R.id.currentPlayer);
				t.clearAnimation();
				if (current < 0) { 
					v.setBackgroundColor(Color.argb(96, 255, 255, 255));
					t.setText("no player");
				} else {
					v.setBackgroundColor(colors[current]);
					if (!local) 
						t.setText(String.format("Waiting for %s", names[current]));
					else {
						t.setText("It's your turn!");
						Animation a = new TranslateAnimation(0, 8, 0, 0);
						a.setInterpolator(new CycleInterpolator(2));
						a.setDuration(500);
						a.setStartOffset(5000);
						a.setRepeatCount(Animation.INFINITE);
//						a.setRepeatMode(Animation.REVERSE);
						t.startAnimation(a);
					}
				}
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
			Player player = client.spiel.get_player(i);
			Log.i(tag, (client.spiel.is_local_player(i) ? "*" : " ") + "Player " + i
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
			if (client.spiel.is_local_player(i))
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
	public boolean commitCurrentStone(final Stone stone, final int x, final int y) {
		Log.w(tag, "commitCurrentStone(" + x + ", " + y + ")");
		if (!client.spiel.is_local_player())
			return false;
		if (client.spiel.is_valid_turn(stone, client.spiel.current_player(), y, x) != Stone.FIELD_ALLOWED)
			return false;
		
		spielthread.post(new Runnable() {
			@Override
			public void run() {
				client.set_stone(stone, y, x);
			}
		});
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
		if (client != null && client.spiel.current_player() >= 0 && lastStatus.clients > 1)
			showDialog(DIALOG_QUIT);
		else
			super.onBackPressed();
	}
}