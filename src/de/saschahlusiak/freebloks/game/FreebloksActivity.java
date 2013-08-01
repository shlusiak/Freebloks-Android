package de.saschahlusiak.freebloks.game;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.JNIServer;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.lobby.ChatEntry;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
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
import de.saschahlusiak.freebloks.view.effects.EffectSet;
import de.saschahlusiak.freebloks.view.effects.StoneFadeEffect;
import de.saschahlusiak.freebloks.view.effects.StoneRollEffect;
import de.saschahlusiak.freebloks.view.model.Intro;
import de.saschahlusiak.freebloks.view.model.Sounds;
import de.saschahlusiak.freebloks.view.model.Theme;
import de.saschahlusiak.freebloks.view.model.Intro.OnIntroCompleteListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FreebloksActivity extends Activity implements ActivityInterface, SpielClientInterface, OnIntroCompleteListener {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_GAME_MENU = 1;
	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_RATE_ME = 4;
	static final int DIALOG_JOIN = 5;
	static final int DIALOG_HOST = 9;
	static final int DIALOG_CUSTOM_GAME = 7;
	static final int DIALOG_NEW_GAME_CONFIRMATION = 8;
	
	static final int REQUEST_FINISH_GAME = 1;

	public static final String GAME_STATE_FILE = "gamestate.bin";

	
//	public static final int KI_PERFECT = 0;
	public static final int KI_HARD = 5;
//	public static final int KI_MEDIUM = 50;
//	public static final int KI_EASY = 120;
	public static final int KI_DEFAULT = KI_HARD;


	Freebloks3DView view;
	SpielClient client = null;
	SpielClientThread spielthread = null;
	Vibrator vibrator;
	boolean vibrate_on_move;
	boolean undo_with_back;
	boolean hasActionBar;
	NET_SERVER_STATUS lastStatus;
	Menu optionsMenu;
	ViewGroup statusView;
	String clientName;
	
	ImageButton chatButton;
	ArrayList<ChatEntry> chatEntries;
	
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
			lastStatus = null;
			view.setSpiel(null, null);
			chatButton.setVisibility(View.INVISIBLE);
			chatEntries.clear();
			progress = new ProgressDialog(FreebloksActivity.this);
			progress.setMessage(getString(R.string.connecting));
			progress.setIndeterminate(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setCancelable(true);
			progress.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				this.myclient.connect(FreebloksActivity.this, params[0], Network.DEFAULT_PORT);
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
				AlertDialog.Builder builder = new AlertDialog.Builder(FreebloksActivity.this);
				builder.setTitle(android.R.string.dialog_alert_title);
				builder.setMessage(result);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showDialog(DIALOG_GAME_MENU);
					}
				});
				builder.setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {						
						showDialog(DIALOG_GAME_MENU);
					}
				});
				builder.create().show();
			} else {
				if (show_lobby)
					showDialog(DIALOG_LOBBY);
				
				myclient.addClientInterface(FreebloksActivity.this);
				newCurrentPlayer(myclient.spiel.current_player());
				/* TODO: there is probably a race condition, when the device is
				 * rotated, the task finishes and a new task is started while
				 * the thread is running.
				 */
				spielthread.start();
			}
			super.onPostExecute(result);
		}
	}

	SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(tag, "onCreate");
		
		prefs = PreferenceManager.getDefaultSharedPreferences(FreebloksActivity.this);
		
		hasActionBar = false;
		/* by default, don't show title bar */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			/* all honeycomb tablets had no menu button; leave action bar visible */
			hasActionBar = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				/* tablets/phone with ICS may or may not have physical buttons. Show action bar if mising */
				ViewConfiguration viewConfig = ViewConfiguration.get(this);
				/* we need the action bar if we don't have a menu key */
				hasActionBar = !viewConfig.hasPermanentMenuKey();
			}
		}
		if (!hasActionBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main_3d);

		view = (Freebloks3DView)findViewById(R.id.board);
		view.setActivity(this);
		
		statusView = (ViewGroup)findViewById(R.id.currentPlayerLayout);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		chatButton = (ImageButton)findViewById(R.id.chatButton);
		chatButton.setVisibility(View.INVISIBLE);
		chatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chatButton.clearAnimation();
				showDialog(DIALOG_LOBBY);
			}
		});
		if (savedInstanceState != null)
			chatEntries = (ArrayList<ChatEntry>)savedInstanceState.getSerializable("chatEntries");
		else
			chatEntries = new ArrayList<ChatEntry>();

		newCurrentPlayer(-1);
		
		RetainedConfig config = (RetainedConfig)getLastNonConfigurationInstance();
		if (config != null) {
			spielthread = config.clientThread;
			lastStatus = config.lastStatus;
			view.model.soundPool = config.soundPool;
			canresume = true;
			chatButton.setVisibility((lastStatus != null && lastStatus.clients > 1) ? View.VISIBLE : View.INVISIBLE);
		}
		if (savedInstanceState != null) {
			view.setScale(savedInstanceState.getFloat("view_scale", 1.0f));
			showRateDialog = savedInstanceState.getBoolean("showRateDialog", false);
		} else {
			view.setScale(prefs.getFloat("view_scale", 1.0f));
			showRateDialog = RateAppDialog.checkShowRateDialog(this);
		}
		if (view.model.soundPool == null)
			view.model.soundPool = new Sounds(this);
		clientName = prefs.getString("player_name", null);

		if (spielthread != null) {
			/* we just rotated and got *hot* objects */
			client = spielthread.client;
			client.addClientInterface(this);
			view.setSpiel(client, client.spiel);
			newCurrentPlayer(client.spiel.current_player());
		} else if (savedInstanceState != null) {
			/* this can happen, when there is no game running and we rotate the device */
			if (!readStateFromBundle(savedInstanceState)) {
				canresume = false;
				newCurrentPlayer(-1);
			}
		} else {
			if (prefs.getBoolean("show_animations", true) && ! prefs.getBoolean("skip_intro", false)) {
				view.model.intro = new Intro(view.model, this);
				newCurrentPlayer(-1);
			} else
				OnIntroCompleted();
		}
		
		statusView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (view.model.intro != null)
					view.model.intro.cancel();
			}
		});
		
		findViewById(R.id.myLocation).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				view.model.board.resetRotation();
			}
		});
		
		
		final Animation a = new TranslateAnimation(0, 8, 0, 0);
		a.setInterpolator(new CycleInterpolator(2));
		a.setDuration(500);
		Runnable r = new Runnable() {	
			@Override
			public void run() {
				boolean local = false;
				View t = findViewById(R.id.currentPlayer);
				t.postDelayed(this, 5000);
				
				if (client != null && client.spiel != null)
					local = client.spiel.is_local_player();
				if (!local)
					return;
				
				t.startAnimation(a);
			}
		};
		findViewById(R.id.currentPlayer).postDelayed(r, 1000);
	}
	
	boolean canresume = false;
	boolean showRateDialog = false;

	@Override
	public void OnIntroCompleted() {
		newCurrentPlayer(-1);
		try {
			if (restoreOldGame()) {
				canresume = true;
			} else {
				canresume = false;
			}
		} catch (Exception e) {
			canresume = false;
			Toast.makeText(FreebloksActivity.this, R.string.could_not_restore_game, Toast.LENGTH_LONG).show();
		}
		
		if (!canresume || ! prefs.getBoolean("auto_resume", false))
			showDialog(DIALOG_GAME_MENU);
		
		if (showRateDialog)
			showDialog(DIALOG_RATE_ME);
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
		if (client != null) {
			client.removeClientInterface(this);
			/* TODO: make attach/detach of view symmetric */
			client.removeClientInterface(view);
		}
		if (view.model.soundPool != null)
			view.model.soundPool.release();
		view.model.soundPool = null;
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		Log.d(tag, "onPause");
		if (client != null && client.spiel.current_player() >= 0 && !client.spiel.is_finished())
			saveGameState(GAME_STATE_FILE);
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d(tag, "onResume");
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		view.onPause();
		Editor editor = prefs.edit();
		editor.putFloat("view_scale", view.getScale());
		editor.commit();
		Log.d(tag, "onStop");
		super.onStop();
	}

	@Override
	protected void onStart() {
		Log.d(tag, "onStart");
		super.onStart();
		view.onResume();
		
		vibrate_on_move = prefs.getBoolean("vibrate", true);
		view.model.soundPool.setEnabled(prefs.getBoolean("sounds", true));
		view.model.showSeeds = prefs.getBoolean("show_seeds", true);
		view.model.showOpponents = prefs.getBoolean("show_opponents", true);
		view.model.showAnimations = prefs.getBoolean("show_animations", true);
		view.model.snapAid = prefs.getBoolean("snap_aid", true);
		undo_with_back = prefs.getBoolean("back_undo", false);
		clientName = prefs.getString("player_name", null);
		if (clientName != null && clientName.equals(""))
			clientName = null;
		/* TODO: maybe put theme in retained config and track for change using a name */
		Theme t = Theme.get(prefs.getString("theme", "texture_wood"), false);
		view.setTheme(t);
		
		updateSoundMenuEntry();
		/* update wheel in case showOpponents has changed */
		view.model.wheel.update(view.model.board.getShowWheelPlayer());
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d(tag, "onRetainNonConfigurationInstance");
		RetainedConfig config = new RetainedConfig();
		config.clientThread = spielthread;
		config.lastStatus = lastStatus;
		config.soundPool = view.model.soundPool;
		view.model.soundPool = null;
		spielthread = null;
		return config;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(tag, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putFloat("view_scale", view.getScale());
		outState.putBoolean("showRateDialog", showRateDialog);
		outState.putSerializable("chatEntries", chatEntries);
		writeStateToBundle(outState);
	}
	
	private void writeStateToBundle(Bundle outState) {
		if (client == null)
			return;
		if (client.spiel == null)
			return;
		Spielleiter l = client.spiel;
		outState.putSerializable("game", l);
		outState.putInt("last_difficulty", client.getLastDifficulty());
	}
	
	private boolean readStateFromBundle(Bundle in) {
		try {
			Spielleiter spiel1 = (Spielleiter)in.getSerializable("game");
			
			JNIServer.runServer(spiel1, spiel1.m_gamemode, spiel1.m_field_size_x, KI_DEFAULT);
			
			/* this will start a new SpielClient, which needs to be restored 
			 * from saved gamestate first */
			SpielClient client = new SpielClient(spiel1, in.getInt("last_difficulty"), null);
			ConnectTask task = new ConnectTask(client, false, null);
			task.execute((String)null);

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	long gameStartTime = 0;
	
	public void startNewGame() {
		int ki = KI_DEFAULT;
		int gamemode = Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS;
		int fieldsize = Spiel.DEFAULT_FIELD_SIZE_X;
		boolean players[] = null;
		
		/* when starting a new game from the options menu, keep previous
		 * selected settings / players, etc.
		 */
		if (client != null && client.spiel != null) {
			ki = client.getLastDifficulty();
			gamemode = client.spiel.m_gamemode;
			fieldsize = client.spiel.m_field_size_x;
			players = client.getLastPlayers();
		}
		startNewGame(null, false, players, gamemode, fieldsize, ki);
	}
	
	public void startNewGame(final String server, final boolean show_lobby, final boolean[] request_player, int gamemode, int field_size, int difficulty) {
		newCurrentPlayer(-1);
		if (server == null) {
			JNIServer.runServer(null, gamemode, field_size, difficulty);
		}
		
		if (spielthread != null)
			spielthread.client.disconnect();
		
		view.model.clearEffects();
		Spielleiter spiel = new Spielleiter(Spiel.DEFAULT_FIELD_SIZE_Y, Spiel.DEFAULT_FIELD_SIZE_X);
		final SpielClient client = new SpielClient(spiel, difficulty, request_player);
		spiel.start_new_game();
		spiel.set_stone_numbers(0, 0, 0, 0, 0);
		
		ConnectTask task = new ConnectTask(client, show_lobby, new Runnable() {
			@Override
			public void run() {
				if (request_player == null)
					client.request_player(-1, clientName);
				else {
					for (int i = 0; i < 4; i++)
						if (request_player[i])
							client.request_player(i, clientName);
				}
				if (! show_lobby)
					client.request_start();
			}
		});
		task.execute(server);
	}
	
	boolean restoreOldGame() throws Exception {
		try {
			FileInputStream fis = openFileInput(FreebloksActivity.GAME_STATE_FILE);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Parcel p = Parcel.obtain();
			byte[] b = new byte[1024];
			int bytesRead;
			while ((bytesRead = fis.read(b)) != -1) {
			   bos.write(b, 0, bytesRead);
			}
			fis.close();
			fis = null;
			
			byte[] bytes = bos.toByteArray();
			bos.close();
			bos = null;
			
			Bundle bundle;
			p.unmarshall(bytes, 0, bytes.length);
			p.setDataPosition(0);
			bundle = p.readBundle(FreebloksActivity.class.getClassLoader());
			p.recycle();
			
			deleteFile(GAME_STATE_FILE);
			
			if (readStateFromBundle(bundle)) {
				return true;
			} else {
				return false;
			}
		} catch (FileNotFoundException fe) {
			/* signal non-failure if game state file is missing */
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}		
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
			p.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_optionsmenu, menu);
		optionsMenu = menu;

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean local = false;
		if (client != null && client.spiel != null)
			local = client.spiel.is_local_player();

		menu.findItem(R.id.undo).setEnabled(local && lastStatus != null && lastStatus.clients <= 1);
		menu.findItem(R.id.hint).setEnabled(local);
		menu.findItem(R.id.sound_toggle_button).setVisible(hasActionBar);
		updateSoundMenuEntry();

		return super.onPrepareOptionsMenu(menu);
	}
	
	void updateSoundMenuEntry() {
		boolean on = true;
		if (optionsMenu == null)
			return;
		if (view != null && view.model != null && view.model.soundPool != null)
			on = view.model.soundPool.isEnabled();
		optionsMenu.findItem(R.id.sound_toggle_button).setTitle(on ? R.string.sound_on : R.string.sound_off);
		optionsMenu.findItem(R.id.sound_toggle_button).setIcon(on ? android.R.drawable.ic_lock_silent_mode_off : android.R.drawable.ic_lock_silent_mode);
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
			}, chatEntries);
			
		case DIALOG_QUIT:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.do_you_want_to_leave_current_game);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					canresume = true;
					showDialog(DIALOG_GAME_MENU);
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			return builder.create();
			
		case DIALOG_NEW_GAME_CONFIRMATION:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.do_you_want_to_leave_current_game);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					startNewGame();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			return builder.create();
			
		case DIALOG_RATE_ME:
			return new RateAppDialog(this);
			
		case DIALOG_GAME_MENU:
			return new GameMenu(this);
			
		case DIALOG_CUSTOM_GAME:
			return new CustomGameDialog(this, new CustomGameDialog.OnStartCustomGameListener() {
				@Override
				public boolean OnStart(CustomGameDialog dialog) {
					startNewGame(
							null,
							false,
							dialog.getPlayers(),
							dialog.getGameMode(),
							dialog.getFieldSize(),
							dialog.getDifficulty());
					dismissDialog(DIALOG_CUSTOM_GAME);
					dismissDialog(DIALOG_GAME_MENU);
					return true;
				}
			});

		case DIALOG_JOIN:
			return new CustomGameDialog(this, new CustomGameDialog.OnStartCustomGameListener() {
				
				@Override
				public boolean OnStart(CustomGameDialog dialog) {
					clientName = dialog.getName();
					startNewGame(
							dialog.getServer(),
							true,
							dialog.getPlayers(),
							/* we are joining, ignore game_mode and field_size */
							Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS,
							Spiel.DEFAULT_FIELD_SIZE_X,
							KI_DEFAULT);
					dismissDialog(DIALOG_GAME_MENU);
					return true;
				}
			});
			
		case DIALOG_HOST:
			return new CustomGameDialog(this, new CustomGameDialog.OnStartCustomGameListener() {
				
				@Override
				public boolean OnStart(CustomGameDialog dialog) {
					clientName = dialog.getName();
					startNewGame(
							null,
							true,
							dialog.getPlayers(),
							dialog.getGameMode(),
							dialog.getFieldSize(),
							/* yeah, when hosting we don't care about computer strength... */
							KI_DEFAULT);
					dismissDialog(DIALOG_GAME_MENU);
					return true;
				}
			});

		default:
			return super.onCreateDialog(id);
		}
	}
	
	@Override
	protected void onPrepareDialog(int id, final Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_LOBBY:
			((LobbyDialog)dialog).setSpiel(client);
			if (lastStatus != null)
				((LobbyDialog)dialog).serverStatus(lastStatus);
			dialog.setOnCancelListener(new OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface arg0) {
					if (client.spiel.current_player() < 0 && !client.spiel.is_finished()) {
						canresume = false;
						client.disconnect();
						showDialog(DIALOG_GAME_MENU);
					}
				}
			});
			break;

		case DIALOG_GAME_MENU:
			dialog.findViewById(R.id.new_game).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					/* starting new game from dialog creates game with default settings */
					startNewGame(
							null,
							false,
							null, 
							Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS, 
							Spiel.DEFAULT_FIELD_SIZE_X,
							KI_DEFAULT);
				}
			});
			dialog.findViewById(R.id.resume_game).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			dialog.findViewById(R.id.star).setVisibility(showRateDialog ? View.VISIBLE : View.GONE);
			dialog.findViewById(R.id.star).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DIALOG_RATE_ME);
				}
			});
//			dialog.findViewById(R.id.sound_toggle_button).setVisibility(hasActionBar ? View.GONE : View.VISIBLE);
			dialog.findViewById(R.id.resume_game).setEnabled(canresume);
			dialog.setCanceledOnTouchOutside(canresume);
			dialog.findViewById(R.id.preferences).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FreebloksActivity.this, FreebloksPreferences.class);
					startActivity(intent);
				}
			});
			dialog.findViewById(R.id.join_game).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DIALOG_JOIN);
				}
			});
			dialog.findViewById(R.id.host_game).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DIALOG_HOST);
				}
			});
			dialog.findViewById(R.id.new_game_custom).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDialog(DIALOG_CUSTOM_GAME);
				}
			});
			dialog.findViewById(R.id.new_game).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showDialog(DIALOG_CUSTOM_GAME);
					return true;
				}
			});
			break;

		case DIALOG_JOIN:
			((CustomGameDialog)dialog).prepareJoinDialog();
			break;
			
		case DIALOG_HOST:
			((CustomGameDialog)dialog).prepareHostDialog();
			break;

		case DIALOG_CUSTOM_GAME:
			((CustomGameDialog)dialog).prepareCustomGameDialog();
			break;
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.new_game:
			if (view.model.intro != null)
				view.model.intro.cancel();
			else {
				if (client == null || (client.spiel != null && client.spiel.is_finished()))
					startNewGame();
				else
					showDialog(DIALOG_NEW_GAME_CONFIRMATION);
			}
			return true;

		case R.id.preferences:
			intent = new Intent(this, FreebloksPreferences.class);
			startActivity(intent);
			return true;
			
		case R.id.sound_toggle_button:
			Editor editor = prefs.edit();
			view.model.soundPool.toggle();
			editor.putBoolean("sounds", view.model.soundPool.isEnabled());
			editor.commit();
			updateSoundMenuEntry();
			Toast.makeText(this, getString(view.model.soundPool.isEnabled() ? R.string.sound_on : R.string.sound_off), Toast.LENGTH_SHORT).show();
			return true;

		case R.id.hint:
			if (client == null)
				return true;
			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			findViewById(R.id.movesLeft).setVisibility(View.INVISIBLE);
			view.model.currentStone.startDragging(null, null);
			spielthread.post(new Runnable() {
				@Override
				public void run() {
					client.request_hint(client.spiel.current_player());
				}
			});
			return true;
			
		case R.id.undo:
			if (client == null)
				return true;
			view.model.clearEffects();
			spielthread.post(new Runnable() {
				@Override
				public void run() {
					client.request_undo();
				}
			});
			view.model.soundPool.play(view.model.soundPool.SOUND_UNDO, 1.0f, 1.0f);
			return true;
			
		case R.id.show_main_menu:
			if (client != null && client.spiel.current_player() >= 0 && lastStatus.clients > 1)
				showDialog(DIALOG_QUIT);
			else {
				if (client != null && client.isConnected())
					canresume = true;
				else
					canresume = false;

				showDialog(DIALOG_GAME_MENU);
				if (view.model.intro != null)
					view.model.intro.cancel();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_FINISH_GAME:
			if (resultCode == GameFinishActivity.RESULT_NEW_GAME) {
				if (client == null) {
					/* TODO: find out why client can be null here */
					/* if we don't have a previous client object, start new game with default values */
					startNewGame(null, false, null, Spielleiter.GAMEMODE_4_COLORS_4_PLAYERS, Spiel.DEFAULT_FIELD_SIZE_X, KI_DEFAULT);
				} else {
					startNewGame(client.getLastHost(),
						client.getLastHost() != null,
						client.getLastPlayers(),
						client.spiel.m_gamemode,
						client.spiel.m_field_size_x,
						client.getLastDifficulty());
				}
			}
			if (resultCode == GameFinishActivity.RESULT_SHOW_MENU) {
				showDialog(DIALOG_GAME_MENU);
			}
			break;
			
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void newCurrentPlayer(final int player) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				boolean local = false;
				int showPlayer = view.model.board.getShowDetailsPlayer();
				if (client != null && client.spiel != null)
					local = client.spiel.is_local_player(player);
				else
					showPlayer = player;
				
				if (optionsMenu != null) {
					optionsMenu.findItem(R.id.hint).setEnabled(local);
					optionsMenu.findItem(R.id.undo).setEnabled(local && lastStatus != null && lastStatus.clients <= 1);
				}

				findViewById(R.id.progressBar).setVisibility((local || player < 0) ? View.GONE : View.VISIBLE);
				
				TextView movesLeft, points, status;
				movesLeft = (TextView)findViewById(R.id.movesLeft);
				movesLeft.setVisibility(View.INVISIBLE);
				points = (TextView)findViewById(R.id.points);
				points.setVisibility(View.INVISIBLE);
 
				status = (TextView)findViewById(R.id.currentPlayer);
				status.clearAnimation();
				findViewById(R.id.myLocation).setVisibility((showPlayer >= 0) ? View.VISIBLE : View.INVISIBLE);
				if (player < 0)
					statusView.setBackgroundColor(Color.rgb(64, 64, 80));
				if (view.model.intro != null)
					status.setText(R.string.touch_to_skip);
				else if (client == null || !client.isConnected())
					status.setText(R.string.not_connected);
				else if (client.spiel.is_finished()) {
					/* TODO: don't display player details in 2 player 2 color mode */
					int pl = view.model.board.getShowWheelPlayer();
					Player p = client.spiel.get_player(pl);
					status.setText("[" + getPlayerName(pl) + "]");
					statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[pl]);
					points.setVisibility(View.VISIBLE);
					points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.m_stone_points, p.m_stone_points));
					movesLeft.setVisibility(View.VISIBLE);
					movesLeft.setText(getResources().getQuantityString(R.plurals.number_of_stones_left, p.m_stone_count, p.m_stone_count));
				} else if (player >= 0 || showPlayer >= 0) {
					if (showPlayer < 0) {
						statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[player]);
						Player p = client.spiel.get_player(player);
						points.setVisibility(View.VISIBLE);
						points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.m_stone_points, p.m_stone_points));
						if (!local) 
							status.setText(getString(R.string.waiting_for_color, getPlayerName(player)));
						else {
							status.setText(getString(R.string.your_turn, getPlayerName(player)));
							
							movesLeft.setVisibility(View.VISIBLE);
							movesLeft.setText(getString(R.string.player_status_moves, p.m_number_of_possible_turns));
						}
					} else {
						statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[showPlayer]);
						Player p = client.spiel.get_player(showPlayer);
						points.setVisibility(View.VISIBLE);
						points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.m_stone_points, p.m_stone_points));
						
						if (p.m_number_of_possible_turns <= 0)
							status.setText("[" + getString(R.string.color_is_out_of_moves, getPlayerName(showPlayer)) + "]");
						else {
							status.setText(getPlayerName(showPlayer));
							
							movesLeft.setVisibility((local || player < 0) ? View.VISIBLE : View.INVISIBLE);
							movesLeft.setText(getString(R.string.player_status_moves, p.m_number_of_possible_turns));
						}
					}

				} else
					status.setText(R.string.no_player);
			}
		});
	}

	@Override
	public void stoneWillBeSet(NET_SET_STONE s) {

	}
	
	@Override
	public void stoneHasBeenSet(final NET_SET_STONE s) {
		if (!client.spiel.is_local_player(s.player)) {
			view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float)Math.random() * 0.2f);
			vibrate(Global.VIBRATE_SET_STONE);
		}
		
		Player p = client.spiel.get_player(s.player);
		/* TODO: also show toast, if previous player blocked out another player */
		if (p.m_number_of_possible_turns <= 0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(FreebloksActivity.this, getString(R.string.color_is_out_of_moves, getPlayerName(s.player)), Toast.LENGTH_SHORT).show();
					view.model.soundPool.play(view.model.soundPool.SOUND_PLAYER_OUT, 0.8f, 1.0f);
				}
			});
		}
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {				
				findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
				findViewById(R.id.movesLeft).setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void gameFinished() {
		deleteFile(GAME_STATE_FILE);
		/* TODO: play sound on game finish */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(FreebloksActivity.this, GameFinishActivity.class);
				intent.putExtra("game", (Serializable)client.spiel);
				intent.putExtra("lastStatus", (Serializable)lastStatus);
				intent.putExtra("clientName", clientName);
				startActivityForResult(intent, REQUEST_FINISH_GAME);
			}
		});
	}

	@Override
	public void chatReceived(final NET_CHAT c) {
		String name = null;
		int player = -1;
		if (lastStatus != null && c.client >= 0) {
			if (lastStatus.isAdvanced())
				for (int i = 0; i < lastStatus.spieler.length; i++)
					if (lastStatus.spieler[i] == c.client) {
						player = i;
						break;
					}
			name = lastStatus.getClientName(getResources(), c.client);
		} else {
			/* if we have advanced status, ignore all server messages (c == -1) */
			/* server messages are generated in serverStatus */
			if (lastStatus != null && lastStatus.isAdvanced())
				return;
			name = getString(R.string.client_d, c.client + 1);
		}
			
		final ChatEntry e = new ChatEntry(c.client, c.text, name);
		e.setPlayer(player);
		
		if (!view.model.spiel.is_local_player(player))
			view.model.soundPool.play(view.model.soundPool.SOUND_CHAT, 0.5f, 1.0f);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				chatEntries.add(e);
				if (c.client == -1)
					Toast.makeText(FreebloksActivity.this, "* " + c.text,
							Toast.LENGTH_LONG).show();
				else if (hasWindowFocus()) {
					/* only animate chatButton, if no dialog has focus */
					/* TODO: animate if activity is stopped or paused? */
					
					Animation a = new AlphaAnimation(0.4f, 1.0f);
					a.setDuration(350);
					a.setRepeatCount(Animation.INFINITE);
					a.setRepeatMode(Animation.REVERSE);
					chatButton.startAnimation(a);
				}
			}
		});
	}

	@Override
	public void gameStarted() {
		gameStartTime = System.currentTimeMillis();

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
		if (lastStatus != null && status != null && lastStatus.isAdvanced()) {
			/* generate server chat messages, aka "joined" and "left" */
			
			for (int i = 0; i < lastStatus.spieler.length; i++) {
				NET_SERVER_STATUS s;
				final int tid;
				if (lastStatus.spieler[i] < 0 && status.spieler[i] >= 0) {
					/* joined */
					s = status;
					tid = R.string.player_joined_color;
				} else if (lastStatus.spieler[i] >= 0 && status.spieler[i] < 0) {
					/* left */
					s = lastStatus;
					tid = R.string.player_left_color;
				} else continue;
				String name;
				name = s.getClientName(getResources(), s.spieler[i]);
				
				final String text = getString(tid, name, getResources().getStringArray(R.array.color_names)[i]); 
				final ChatEntry e = new ChatEntry(-1, text, name);
				e.setPlayer(i);
			
				if (!view.model.spiel.is_local_player(i))
					view.model.soundPool.play(view.model.soundPool.SOUND_CHAT, 0.5f, 1.0f);
			
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						chatEntries.add(e);
						/* only show toast on "left" */
						if (tid == R.string.player_left_color)
							Toast.makeText(FreebloksActivity.this, "* " + text, Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		lastStatus = status;
		if (lastStatus.clients > 1) {
			chatButton.post(new Runnable() {
				@Override
				public void run() {
					chatButton.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onConnected(Spiel spiel) {

	}

	@Override
	public void onDisconnected(Spiel spiel) {
		Log.w(tag, "onDisconnected()");
		final Exception error = spielthread == null ? null : spielthread.getError();

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				newCurrentPlayer(-1);

				if (error != null) {
					/* TODO: add sound on disconnect on error */
					saveGameState(GAME_STATE_FILE);

					AlertDialog.Builder builder = new AlertDialog.Builder(FreebloksActivity.this);
					builder.setTitle(android.R.string.dialog_alert_title);
					builder.setMessage(getString(R.string.disconnect_error, error.getMessage()));
					builder.setIcon(android.R.drawable.ic_dialog_alert);
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							try {
								canresume = restoreOldGame();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					builder.create().show();
				}
			}
		});
	}

	@Override
	public boolean commitCurrentStone(final Stone stone, final int x, final int y) {
		if (!client.spiel.is_local_player())
			return false;
		if (client.spiel.is_valid_turn(stone, client.spiel.current_player(), y, x) != Stone.FIELD_ALLOWED)
			return false;
		
		if (view.model.showAnimations) {
			Stone st = new Stone();
			int player = client.spiel.current_player();
			st.copyFrom(stone);
			StoneRollEffect e = new StoneRollEffect(view.model, st, player, x, y, view.model.currentStone.hover_height_high, -15.0f);
	
			EffectSet set = new EffectSet();
			set.add(e);
			set.add(new StoneFadeEffect(view.model, st, player, x, y, 4.0f));
			view.model.addEffect(set);
		}

		view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float)Math.random() * 0.2f);
		vibrate(Global.VIBRATE_SET_STONE);		
		
		spielthread.post(new Runnable() {
			@Override
			public void run() {
				client.set_stone(stone, y, x);
			}
		});
		return true;
	}
	
	@Override
	public void vibrate(int ms) {
		if (vibrate_on_move)
			vibrator.vibrate(ms);
	}
	
	@Override
	public void onBackPressed() {
		if (undo_with_back && client != null && client.isConnected()) {
			view.model.clearEffects();

			spielthread.post(new Runnable() {
				@Override
				public void run() {
					client.request_undo();
				}
			});
			view.model.soundPool.play(view.model.soundPool.SOUND_UNDO, 1.0f, 1.0f);
			return;
		}
		if (client != null && client.spiel.current_player() >= 0 && lastStatus.clients > 1)
			showDialog(DIALOG_QUIT);
		else {
			if (view.model.intro != null) {
				view.model.intro.cancel();
			}
			else {
				if (client != null && client.isConnected())
					canresume = true;
				else
					canresume = false;
				showDialog(DIALOG_GAME_MENU);
			}
		}
	}

	@Override
	public void showPlayer(int player) {
		if (client == null)
			return;
		if (client.spiel == null)
			return;
		newCurrentPlayer(client.spiel.current_player());
	}
	
	String getPlayerName(int player) {
		String color_name = getResources().getStringArray(R.array.color_names)[player];
		/* this will ensure that always the local name is used, even though the server
		 * might still have stored an old or no name at all
		 * 
		 * When resuming a game, the name is lost and never set again. This is a non issue now.
		 */
		if (clientName != null && clientName.length() > 0 && client != null && client.spiel != null && client.spiel.is_local_player(player))
			return clientName;
		if (lastStatus == null)
			return color_name;
		return lastStatus.getPlayerName(getResources(), player);
	}	
}
