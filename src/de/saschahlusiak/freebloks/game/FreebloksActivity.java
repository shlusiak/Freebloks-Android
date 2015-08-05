package de.saschahlusiak.freebloks.game;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.google.example.games.basegameutils.BaseGameActivity;

import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.GameMode;
import de.saschahlusiak.freebloks.controller.JNIServer;
import de.saschahlusiak.freebloks.controller.PlayerData;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.controller.Spielleiter;
import de.saschahlusiak.freebloks.donate.DonateActivity;
import de.saschahlusiak.freebloks.lobby.ChatEntry;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.BoardStoneGlowEffect;
import de.saschahlusiak.freebloks.view.effects.Effect;
import de.saschahlusiak.freebloks.view.effects.EffectSet;
import de.saschahlusiak.freebloks.view.effects.StoneFadeEffect;
import de.saschahlusiak.freebloks.view.effects.StoneRollEffect;
import de.saschahlusiak.freebloks.view.model.Intro;
import de.saschahlusiak.freebloks.view.model.Sounds;
import de.saschahlusiak.freebloks.view.model.Theme;
import de.saschahlusiak.freebloks.view.model.ViewModel;
import de.saschahlusiak.freebloks.view.model.Intro.OnIntroCompleteListener;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StrictMode;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FreebloksActivity extends BaseGameActivity implements ActivityInterface, SpielClientInterface, OnIntroCompleteListener {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_GAME_MENU = 1;
	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_RATE_ME = 4;
	static final int DIALOG_JOIN = 5;
	static final int DIALOG_PROGRESS = 6;
	static final int DIALOG_CUSTOM_GAME = 7;
	static final int DIALOG_NEW_GAME_CONFIRMATION = 8;
	static final int DIALOG_HOST = 9;
	static final int DIALOG_SINGLE_PLAYER = 10;

	static final int REQUEST_FINISH_GAME = 1;

	static final int NOTIFICATION_GAME_ID = 1;

	public static final String GAME_STATE_FILE = "gamestate.bin";


	Freebloks3DView view;
	SpielClient client = null;
	SpielClientThread spielthread = null;
	Vibrator vibrator;
	boolean vibrate_on_move;
	boolean show_notifications;
	boolean undo_with_back;
	boolean hasActionBar;
	NET_SERVER_STATUS lastStatus;
	Menu optionsMenu;
	ViewGroup statusView;
	NotificationManager notificationManager;
	Notification multiplayerNotification;

	ConnectTask connectTask;

	String clientName;
	int difficulty;
	GameMode gamemode;
	int fieldsize;

	ImageButton chatButton;
	ArrayList<ChatEntry> chatEntries;

	SharedPreferences prefs;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");

		if (Build.VERSION.SDK_INT >= 11 && BuildConfig.DEBUG) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	         		 .detectCustomSlowCalls()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   // or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .detectAll()
	                 .penaltyLog()
	                 .build());
	    }

		hasActionBar = false;
		/* by default, don't show title bar */
		if (Build.VERSION.SDK_INT >= 11) {
			/* all honeycomb tablets had no menu button; leave action bar visible */
			hasActionBar = true;
			if (Build.VERSION.SDK_INT >= 14) {
				/* tablets/phone with ICS may or may not have physical buttons. Show action bar if mising */
				ViewConfiguration viewConfig = ViewConfiguration.get(this);
				/* we need the action bar if we don't have a menu key */
				hasActionBar = !viewConfig.hasPermanentMenuKey();
			}
		}

		if (!hasActionBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		else
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		getGameHelper().setMaxAutoSignInAttempts(0);
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        Window w = getWindow();
	        
	        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//	        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
	    }

		if (hasActionBar) {
			if (Build.VERSION.SDK_INT >= 14) {
				getActionBar().setHomeButtonEnabled(true);
				getActionBar().setDisplayHomeAsUpEnabled(true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				} else {
					getActionBar().setIcon(android.R.drawable.ic_dialog_dialer);
				}
			} else
				getActionBar().setDisplayShowHomeEnabled(true);
				
//			getActionBar().setDisplayShowHomeEnabled(true);
//			getActionBar().setDisplayUseLogoEnabled(false);
//			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(104, 0, 0, 0)));
			getActionBar().setBackgroundDrawable(new ColorDrawable(0));
			getActionBar().setDisplayShowTitleEnabled(false);
		}

		setContentView(R.layout.main_3d);

		prefs = PreferenceManager.getDefaultSharedPreferences(FreebloksActivity.this);
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		view = (Freebloks3DView)findViewById(R.id.board);
		view.setActivity(this);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && prefs.getBoolean("immersive_mode", true)) {
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}


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
			view.model.intro = config.intro;
			connectTask = config.connectTask;
			if (connectTask != null)
				connectTask.setActivity(this);
			if (view.model.intro != null)
				view.model.intro.setModel(view.model, this);
			canresume = true;
			chatButton.setVisibility((lastStatus != null && lastStatus.clients > 1) ? View.VISIBLE : View.INVISIBLE);
		}
		if (savedInstanceState != null) {
			view.setScale(savedInstanceState.getFloat("view_scale", 1.0f));
			showRateDialog = savedInstanceState.getBoolean("showRateDialog", false);
		} else {
			view.setScale(prefs.getFloat("view_scale", 1.0f));
			showRateDialog = RateAppDialog.checkShowRateDialog(this);
			if (prefs.getLong("rate_number_of_starts", 0) == Global.DONATE_STARTS) {
				Intent intent = new Intent(this, DonateActivity.class);
				startActivity(intent);
			}
		}
		if (view.model.soundPool == null)
			view.model.soundPool = new Sounds(this);

		clientName = prefs.getString("player_name", null);
		difficulty = prefs.getInt("difficulty", 10);	/* TODO: generalize the value */
		gamemode = GameMode.from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal()));
		fieldsize = prefs.getInt("fieldsize", Spiel.DEFAULT_FIELD_SIZE_X);

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
		notificationManager.cancelAll();
		notificationManager.cancel(NOTIFICATION_GAME_ID);

		if (connectTask != null) try {
			connectTask.cancel(true);
			connectTask.get();
			connectTask = null;
		} catch (Exception e) {
		//	e.printStackTrace();
		}
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
		if (client != null && client.spiel.isStarted() && !client.spiel.isFinished())
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
		if (client != null && client.isConnected()) {
			if ((lastStatus != null && lastStatus.clients > 1) ||
				(!client.spiel.isStarted()))
				updateMultiplayerNotification(true, null);
		}
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

		notificationManager.cancel(NOTIFICATION_GAME_ID);
		multiplayerNotification = null;

		vibrate_on_move = prefs.getBoolean("vibrate", true);
		view.model.soundPool.setEnabled(prefs.getBoolean("sounds", true));
		show_notifications = prefs.getBoolean("notifications", true);
		view.model.showSeeds = prefs.getBoolean("show_seeds", true);
		view.model.showOpponents = prefs.getBoolean("show_opponents", true);
		view.model.showAnimations = Integer.parseInt(prefs.getString("animations", String.format("%d", ViewModel.ANIMATIONS_FULL)));
		view.model.snapAid = prefs.getBoolean("snap_aid", true);
		view.model.immersiveMode = prefs.getBoolean("immersive_mode", true);
		undo_with_back = prefs.getBoolean("back_undo", false);
		clientName = prefs.getString("player_name", null);
		if (clientName != null && clientName.equals(""))
			clientName = null;
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
		config.intro = view.model.intro;
		config.connectTask = connectTask;
		this.connectTask = null;
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
		synchronized (client) {
			Spielleiter l = client.spiel;
			outState.putSerializable("game", l);
		}
	}

	private boolean readStateFromBundle(Bundle in) {
		try {
			Spielleiter spiel1 = (Spielleiter)in.getSerializable("game");
			if (spiel1 == null)
				return false;

			JNIServer.runServer(spiel1, spiel1.m_gamemode.ordinal(), spiel1.m_field_size_x, difficulty);

			/* this will start a new SpielClient, which needs to be restored
			 * from saved gamestate first */
			SpielClient client = new SpielClient(spiel1, difficulty, null, spiel1.m_field_size_x);
			client.spiel.setStarted(true);
			spielthread = new SpielClientThread(client);
			
			connectTask = new ConnectTask(client, false, null);
			connectTask.setActivity(this);
			connectTask.execute((String)null);

			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	long gameStartTime = 0;

	public void startNewGame() {
		boolean players[] = null;

		/* when starting a new game from the options menu, keep previous
		 * selected settings / players, etc.
		 */
		if (client != null && client.spiel != null) {
			players = client.getLastPlayers();
		}
		startNewGame(null, false, players);
	}

	public void startNewGame(final String server, final boolean show_lobby, final boolean[] request_player) {
		newCurrentPlayer(-1);
		if (server == null) {
			JNIServer.runServer(null, gamemode.ordinal(), fieldsize, difficulty);
		}

		if (spielthread != null)
			spielthread.client.disconnect();

		view.model.clearEffects();
		Spielleiter spiel = new Spielleiter(fieldsize, fieldsize);
		final SpielClient client = new SpielClient(spiel, difficulty, request_player, fieldsize);
		spiel.start_new_game(gamemode);
		spiel.set_stone_numbers(0, 0, 0, 0, 0);
		
		spielthread = new SpielClientThread(client);

		connectTask = new ConnectTask(client, show_lobby, new Runnable() {
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
		connectTask.setActivity(this);
		connectTask.execute(server);
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

	private void saveGameState(final String filename) {
		final Parcel p = Parcel.obtain();
		Bundle b = new Bundle();
		writeStateToBundle(b);
		p.writeBundle(b);
		new Thread() {
			public void run() {
				try {
					FileOutputStream fos;
					fos = openFileOutput(filename, Context.MODE_PRIVATE);
					fos.write(p.marshall());
					fos.flush();
					fos.close();
					p.recycle();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
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
			if (client == null)
				return null;
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
					difficulty = dialog.getDifficulty();
					gamemode = dialog.getGameMode();
					fieldsize = dialog.getFieldSize();
					startNewGame(
							null,
							false,
							dialog.getPlayers());
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
					gamemode = dialog.getGameMode();
					startNewGame(
							dialog.getServer(),
							true,
							dialog.getPlayers());
					dismissDialog(DIALOG_GAME_MENU);
					return true;
				}
			});

		case DIALOG_HOST:
			return new CustomGameDialog(this, new CustomGameDialog.OnStartCustomGameListener() {
				@Override
				public boolean OnStart(CustomGameDialog dialog) {
					clientName = dialog.getName();
					gamemode = dialog.getGameMode();
					fieldsize = dialog.getFieldSize();
					/* TODO: host should have configurable difficulty */
					startNewGame(
							null,
							true,
							dialog.getPlayers());
					dismissDialog(DIALOG_GAME_MENU);
					return true;
				}
			});
			
		case DIALOG_PROGRESS:
			ProgressDialog p = new ProgressDialog(FreebloksActivity.this);
			p.setMessage(getString(R.string.connecting));
			p.setIndeterminate(true);
			p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			p.setCancelable(true);
			p.setButton(Dialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			return p;
		
		case DIALOG_SINGLE_PLAYER:
			ColorListDialog d = new ColorListDialog(this, 
					new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int which) {
		            	   boolean[] players = new boolean[4];
		            	   
		            	   if (which == -1)
		            		   players = null;
		            	   else
		            		   players[which] = true;
		            	   
		            	   ColorListDialog d = (ColorListDialog)dialog;
		            	   gamemode = d.getGameMode();
		            	   fieldsize = d.getBoardSize();
		            	   startNewGame(null, false, players);
		            	   
		            	   dialog.dismiss();
		               }
		           });
			d.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						showDialog(DIALOG_GAME_MENU);
					}
				});
			
			return d;
			
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog, Bundle args) {
		switch (id) {
		case DIALOG_LOBBY:
			if (client != null) {
				((LobbyDialog)dialog).setSpiel(client);
				if (lastStatus != null)
					((LobbyDialog)dialog).serverStatus(lastStatus);
				dialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						if (!client.spiel.isStarted() && !client.spiel.isFinished()) {
							canresume = false;
							client.disconnect();
							showDialog(DIALOG_GAME_MENU);
						}
					}
				});
			} else {
				/* this can happen when the app is saved but purged from memory
				 * upon resume, the open dialog is reopened but the client connection
				 * has to be disconnected. just close the lobby since there is no
				 * connection
				 */
				dialog.dismiss();
				canresume = false;
				showDialog(DIALOG_GAME_MENU);
			}
			break;

		case DIALOG_GAME_MENU:
			dialog.findViewById(R.id.new_game).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					showDialog(DIALOG_SINGLE_PLAYER);
				}
			});
			dialog.findViewById(R.id.new_game).setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					dialog.dismiss();

					/* starting new game from dialog creates game with previous settings */
					startNewGame(
							null,
							false,
							null);
					
					return true;
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
			break;

		case DIALOG_JOIN:
			((CustomGameDialog)dialog).prepareJoinDialog(clientName, difficulty, GameMode.GAMEMODE_4_COLORS_4_PLAYERS, fieldsize);
			break;

		case DIALOG_HOST:
			((CustomGameDialog)dialog).prepareHostDialog(clientName, difficulty, gamemode, fieldsize);
			break;

		case DIALOG_CUSTOM_GAME:
			((CustomGameDialog)dialog).prepareCustomGameDialog(clientName, difficulty, gamemode, fieldsize);
			break;
			
		case DIALOG_PROGRESS:
			if (connectTask != null)
				dialog.setOnCancelListener(connectTask);
			break;

		case DIALOG_SINGLE_PLAYER:
			ColorListDialog d = (ColorListDialog)dialog;
			d.setGameMode(gamemode);
			break;
			
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			if (client != null && client.isConnected())
				canresume = true;
			else
				canresume = false;

			showDialog(DIALOG_GAME_MENU);
			if (view.model.intro != null)
				view.model.intro.cancel();
			return true;

		case R.id.new_game:
			if (view.model.intro != null)
				view.model.intro.cancel();
			else {
				if (client == null || (client.spiel != null && client.spiel.isFinished()))
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
			view.model.currentStone.stopDragging();
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
			if (client != null && client.spiel.isStarted() && lastStatus != null && lastStatus.clients > 1)
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
					startNewGame(null, false, null);
				} else {
					startNewGame(
						client.getLastHost(),
						client.getLastHost() != null,
						client.getLastPlayers());
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
				if (multiplayerNotification != null)
					updateMultiplayerNotification(false, null);
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
				else if (client.spiel.isFinished()) {
					int pl = view.model.board.getShowWheelPlayer();
					Player p = client.spiel.get_player(pl);
					status.setText("[" + getPlayerName(pl) + "]");
					statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[view.model.getPlayerColor(pl)]);
					points.setVisibility(View.VISIBLE);
					points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.m_stone_points, p.m_stone_points));
					movesLeft.setVisibility(View.VISIBLE);
					movesLeft.setText(getResources().getQuantityString(R.plurals.number_of_stones_left, p.m_stone_count, p.m_stone_count));
				} else if (player >= 0 || showPlayer >= 0) {
					if (showPlayer < 0) {
						statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[view.model.getPlayerColor(player)]);
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
						statusView.setBackgroundColor(Global.PLAYER_BACKGROUND_COLOR[view.model.getPlayerColor(showPlayer)]);
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

	/* we have to store the number of possible turns before and after a stone has been set
	 * to detect blocking of other players */
	private int number_of_possible_turns[] = new int[4];

	@Override
	public void stoneWillBeSet(NET_SET_STONE s) {
		for (int i = 0; i < 4; i++)
			number_of_possible_turns[i] = client.spiel.get_player(i).m_number_of_possible_turns;
	}

	@Override
	public void stoneHasBeenSet(final NET_SET_STONE s) {
		if (client == null)
			return;
		if (view == null)
			return;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!client.spiel.is_local_player(s.player)) {
					if (view == null)
						return;
					if (view.model.soundPool == null)
						return;
					view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float)Math.random() * 0.2f);
					vibrate(Global.VIBRATE_SET_STONE);
				}
			}
		});

		for (int i = 0; i < 4; i++) {
			final Player p = client.spiel.get_player(i);
			if (p.m_number_of_possible_turns <= 0 && number_of_possible_turns[i] > 0) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(FreebloksActivity.this, getString(R.string.color_is_out_of_moves, getPlayerName(p.getPlayerNumber())), Toast.LENGTH_SHORT).show();
						if (view != null) {
							if (view.model.soundPool != null)
								view.model.soundPool.play(view.model.soundPool.SOUND_PLAYER_OUT, 0.8f, 1.0f);
							if (view.model.hasAnimations()) {
								int sx, sy;
								sx = client.spiel.get_player_start_x(p.getPlayerNumber());
								sy = client.spiel.get_player_start_y(p.getPlayerNumber());
								for (int x = 0; x < client.spiel.m_field_size_x; x++)
									for (int y = 0; y < client.spiel.m_field_size_y; y++)
										if (client.spiel.get_game_field(y, x) == p.getPlayerNumber()) {
											boolean effected = false;
											synchronized (view.model.effects) {
												for (int j = 0; j < view.model.effects.size(); j++)
													if (view.model.effects.get(j).isEffected(x, y)) {
														effected = true;
														break;
													}
											}
											if (!effected) {
												final float distance = (float)Math.sqrt((x - sx)*(x - sx) + (y - sy)*(y - sy));
												Effect effect = new BoardStoneGlowEffect(
														view.model,
														view.model.getPlayerColor(p.getPlayerNumber()),
														x,
														y,
														distance);
												view.model.addEffect(effect);
											}
										}
							}
						}
					}
				});
			}
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
				updateMultiplayerNotification(false, null);
				PlayerData[] data = client.spiel.getResultData();
				new AddScoreTask(getApplicationContext(), client.spiel.m_gamemode).execute(data);

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
			if (lastStatus.isVersion(2))
				for (int i = 0; i < lastStatus.spieler.length; i++)
					if (lastStatus.spieler[i] == c.client) {
						player = i;
						break;
					}
			name = lastStatus.getClientName(getResources(), c.client);
		} else {
			/* if we have advanced status, ignore all server messages (c == -1) */
			/* server messages are generated in serverStatus */
			if (lastStatus != null && lastStatus.isVersion(2))
				return;
			name = getString(R.string.client_d, c.client + 1);
		}

		final ChatEntry e = new ChatEntry(c.client, c.text, name);
		e.setPlayer(player);

		if (!client.spiel.is_local_player(player) &&
			(client.spiel.isStarted() || multiplayerNotification != null))
			updateMultiplayerNotification(true, e.toString());

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
	public void stoneUndone(Turn t) {

	}

	@Override
	public void serverStatus(NET_SERVER_STATUS status) {
		if (lastStatus != null && status != null && lastStatus.isVersion(2)) {
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

				final String text = getString(tid, name, getResources().getStringArray(R.array.color_names)[view.model.getPlayerColor(i)]);
				final ChatEntry e = new ChatEntry(-1, text, name);
				e.setPlayer(i);
				
				if (view == null)
					return;
				if (view.model == null)
					return;
				if (view.model.spiel == null)
					return;

				if (!view.model.spiel.is_local_player(i))
					updateMultiplayerNotification(tid == R.string.player_left_color && client.spiel.isStarted(), text);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						chatEntries.add(e);
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
	public boolean commitCurrentStone(final Turn turn) {
		if (client == null)
			return false;
		
		if (!client.spiel.is_local_player())
			return false;
		if (client.spiel.is_valid_turn(turn) != Stone.FIELD_ALLOWED)
			return false;

		if (view.model.hasAnimations()) {
			Stone st = new Stone();
			st.copyFrom(client.spiel.get_player(turn.m_playernumber).get_stone(turn.m_stone_number));
			StoneRollEffect e = new StoneRollEffect(view.model, turn, view.model.currentStone.hover_height_high, -15.0f);

			EffectSet set = new EffectSet();
			set.add(e);
			set.add(new StoneFadeEffect(view.model, turn, 1.0f));
			view.model.addEffect(set);
		}

		view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float)Math.random() * 0.2f);
		vibrate(Global.VIBRATE_SET_STONE);

		spielthread.post(new Runnable() {
			@Override
			public void run() {
				client.set_stone(turn);
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
		if (client != null && client.spiel.isStarted() && !client.spiel.isFinished() && lastStatus != null && lastStatus.clients > 1)
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
		String color_name = getResources().getStringArray(R.array.color_names)[view.model.getPlayerColor(player)];
		/* this will ensure that always the local name is used, even though the server
		 * might still have stored an old or no name at all
		 *
		 * When resuming a game, the name is lost and never set again. This is a non issue now.
		 */
		if (clientName != null && clientName.length() > 0 && client != null && client.spiel != null && client.spiel.is_local_player(player))
			return clientName;
		if (lastStatus == null)
			return color_name;
		return lastStatus.getPlayerName(getResources(), player, view.model.getPlayerColor(player));
	}

	void updateMultiplayerNotification(boolean forceShow, String chat) {
		if (client == null || client.spiel == null)
			return;
		if (!client.isConnected())
			return;
		if (multiplayerNotification == null && !forceShow)
			return;
		if (!show_notifications)
			return;

		Notification n = new Notification();

		Intent intent = new Intent(this, FreebloksActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		if (chat != null)
			intent.putExtra("showChat", true);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		n.flags = Notification.FLAG_AUTO_CANCEL;
		if (forceShow && chat == null)
			n.flags |= Notification.FLAG_ONGOING_EVENT;
		if (multiplayerNotification != null)
			n.flags |= Notification.FLAG_ONGOING_EVENT;

		if (!client.spiel.isStarted()) {
			n.icon = R.drawable.notification_waiting;
			n.setLatestEventInfo(this,
					getString(R.string.app_name),
					getString(R.string.lobby_waiting_for_players),
					pendingIntent);
		} else if (client.spiel.isFinished()) {
			n.icon = R.drawable.notification_main;
			n.setLatestEventInfo(this,
					getString(R.string.app_name),
					getString(R.string.game_finished),
					pendingIntent);
		} else {
			if (client.spiel.current_player() < 0)
				return;
			if (client.spiel.is_local_player()) {
				n.icon = R.drawable.notification_your_turn;
				n.setLatestEventInfo(this,
						getString(R.string.app_name),
						getString(R.string.your_turn, getPlayerName(client.spiel.current_player())),
						pendingIntent);
				if (!forceShow) {
					n.tickerText = getString(R.string.your_turn, getPlayerName(client.spiel.current_player()));
					n.defaults = Notification.DEFAULT_VIBRATE;
				}
			} else {
				n.icon = R.drawable.notification_waiting;
				n.setLatestEventInfo(this,
						getString(R.string.app_name),
						getString(R.string.waiting_for_color, getPlayerName(client.spiel.current_player())),
						pendingIntent);
			}
		}
		if (chat != null) {
			n.icon = R.drawable.notification_chat;
			n.setLatestEventInfo(this,
					getString(R.string.app_name),
					chat,
					pendingIntent);
			n.tickerText = chat;
			n.defaults = Notification.DEFAULT_VIBRATE;
			n.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chat);
		} else
			multiplayerNotification = n;

		notificationManager.notify(NOTIFICATION_GAME_ID, n);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent.hasExtra("showChat") && client != null && client.spiel.isStarted())
			showDialog(DIALOG_LOBBY);
		super.onNewIntent(intent);
	}

	@Override
	public void onSignInFailed() {

	}

	@Override
	public void onSignInSucceeded() {

	}
}
