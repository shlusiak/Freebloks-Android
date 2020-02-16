package de.saschahlusiak.freebloks.game;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.*;
import android.bluetooth.BluetoothSocket;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.UiThread;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.bluetooth.BluetoothClientToSocketThread;
import de.saschahlusiak.freebloks.bluetooth.BluetoothServerThread;
import de.saschahlusiak.freebloks.client.GameClient;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.client.JNIServer;
import de.saschahlusiak.freebloks.model.PlayerScore;
import de.saschahlusiak.freebloks.client.GameEventObserver;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.donate.DonateActivity;
import de.saschahlusiak.freebloks.lobby.ChatEntry;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;
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

import io.fabric.sdk.android.Fabric;

public class FreebloksActivity extends BaseGameActivity implements ActivityInterface, GameEventObserver, OnIntroCompleteListener {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_GAME_MENU = 1;
	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_RATE_ME = 4;
	static final int DIALOG_JOIN = 5;
	static final int DIALOG_PROGRESS = 6;
	static final int DIALOG_CUSTOM_GAME = 7;
	static final int DIALOG_NEW_GAME_CONFIRMATION = 8;
	static final int DIALOG_SINGLE_PLAYER = 10;

	static final int REQUEST_FINISH_GAME = 1;

	static final int NOTIFICATION_GAME_ID = 1;

	public static final String GAME_STATE_FILE = "gamestate.bin";


	Freebloks3DView view;
	GameClient client = null;
	Vibrator vibrator;
	boolean vibrate_on_move;
	boolean show_notifications;
	boolean undo_with_back;
	boolean hasActionBar;
	MessageServerStatus lastStatus;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");

		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectCustomSlowCalls()
				.detectNetwork()
				.penaltyDeath()
				.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects()
				.detectActivityLeaks()
				.penaltyLog()
//				 .penaltyDeath()
				.build());

		}

		Crashlytics crashlyticsKit = new Crashlytics.Builder()
			.core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
			.build();

		Fabric.with(this, crashlyticsKit);

		Log.d(tag, "nativeLibraryDir=" + getApplicationInfo().nativeLibraryDir);


		/* tablets/phone with ICS may or may not have physical buttons. Show action bar if mising */
		ViewConfiguration viewConfig = ViewConfiguration.get(this);
		/* we need the action bar if we don't have a menu key */
		hasActionBar = !viewConfig.hasPermanentMenuKey();

		if (!hasActionBar)
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		else
			requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);

		Window w = getWindow();

		w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//	        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

		if (hasActionBar) {
			// failsafe, there might be Android versions >= 3.0 without an actual ActionBar
			if (getActionBar() == null)
				hasActionBar = false;
		}

		if (hasActionBar) {
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);

//			getActionBar().setDisplayShowHomeEnabled(true);
//			getActionBar().setDisplayUseLogoEnabled(false);
//			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(104, 0, 0, 0)));
			getActionBar().setBackgroundDrawable(new ColorDrawable(0));
			getActionBar().setDisplayShowTitleEnabled(false);
		}

		setContentView(R.layout.main_3d);

		prefs = PreferenceManager.getDefaultSharedPreferences(FreebloksActivity.this);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		view = findViewById(R.id.board);
		view.setActivity(this);

		if (prefs.getBoolean("immersive_mode", true))
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);


		statusView = findViewById(R.id.currentPlayerLayout);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		chatButton = findViewById(R.id.chatButton);
		chatButton.setVisibility(View.INVISIBLE);
		chatButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				chatButton.clearAnimation();
				showDialog(DIALOG_LOBBY);
			}
		});
		if (savedInstanceState != null)
			chatEntries = (ArrayList<ChatEntry>) savedInstanceState.getSerializable("chatEntries");
		else
			chatEntries = new ArrayList<>();

		newCurrentPlayer(-1);

		RetainedConfig config = (RetainedConfig) getLastNonConfigurationInstance();
		if (config != null) {
			client = config.client;
			lastStatus = config.lastStatus;
			view.model.soundPool = config.soundPool;
			view.model.intro = config.intro;
			connectTask = config.connectTask;
			if (connectTask != null)
				connectTask.setActivity(this);
			if (view.model.intro != null)
				view.model.intro.setModel(view.model, this);
			canresume = true;
			chatButton.setVisibility((lastStatus != null && lastStatus.getClients() > 1) ? View.VISIBLE : View.INVISIBLE);
		}
		if (savedInstanceState != null) {
			view.setScale(savedInstanceState.getFloat("view_scale", 1.0f));
			showRateDialog = savedInstanceState.getBoolean("showRateDialog", false);
		} else {
			view.setScale(prefs.getFloat("view_scale", 1.0f));
			showRateDialog = RateAppDialog.checkShowRateDialog(this);

			long starts = prefs.getLong("rate_number_of_starts", 0);

			if (!Global.IS_VIP && starts == Global.DONATE_STARTS) {
				Intent intent = new Intent(this, DonateActivity.class);
				startActivity(intent);
			}
		}

		if (view.model.soundPool == null)
			view.model.soundPool = new Sounds(getApplicationContext());

		clientName = prefs.getString("player_name", null);
		difficulty = prefs.getInt("difficulty", GameConfiguration.DEFAULT_DIFFICULTY);
		gamemode = GameMode.from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal()));
		fieldsize = prefs.getInt("fieldsize", Board.DEFAULT_BOARD_SIZE);

		if (client != null) {
			/* we just rotated and got *hot* objects */
			client.addObserver(this);
			client.addObserver(view);
			view.setGame(client.game);
			newCurrentPlayer(client.game.getCurrentPlayer());
		} else if (savedInstanceState == null) {
			if (prefs.getBoolean("show_animations", true) && !prefs.getBoolean("skip_intro", false)) {
				view.model.intro = new Intro(getApplicationContext(), view.model, this);
				newCurrentPlayer(-1);
			} else
				OnIntroCompleted();
		}

		statusView.setOnClickListener(v -> {
			if (view.model.intro != null)
				view.model.intro.cancel();
		});

		findViewById(R.id.myLocation).setOnClickListener(v -> view.model.boardObject.resetRotation());


		final Animation a = new TranslateAnimation(0, 8, 0, 0);
		a.setInterpolator(new CycleInterpolator(2));
		a.setDuration(500);
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (view == null)
					return;
				boolean local = false;
				View t = findViewById(R.id.currentPlayer);
				t.postDelayed(this, 5000);

				if (client != null && client.game != null)
					local = client.game.isLocalPlayer();
				if (!local)
					return;

				t.startAnimation(a);
			}
		};
		findViewById(R.id.currentPlayer).postDelayed(r, 1000);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(tag, "onRestoreInstanceState (bundle=" + savedInstanceState + ")");

		if (client == null) {
			if (!readStateFromBundle(savedInstanceState)) {
				canresume = false;
				newCurrentPlayer(-1);
			} else {
				canresume = true;
			}
		}
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

		if (!canresume || !prefs.getBoolean("auto_resume", false))
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
		if (client != null) {
			client.disconnect();
			client = null;
		}
		if (view.model.soundPool != null)
			view.model.soundPool.release();
		view.model.soundPool = null;
		view = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d(tag, "onPause");
		if (client != null && client.game.isStarted() && !client.game.isFinished())
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
		if (!isFinishing() && client != null && client.isConnected()) {
			if ((lastStatus != null && lastStatus.getClients() > 1) ||
				(!client.game.isStarted()))
				updateMultiplayerNotification(true, null);
		}
		view.onPause();
		Editor editor = prefs.edit();
		editor.putFloat("view_scale", view.getScale());
		editor.apply();
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
		Theme t = Theme.get(this, prefs.getString("theme", "texture_wood"), false);
		view.setTheme(t);

		updateSoundMenuEntry();
		/* update wheel in case showOpponents has changed */
		view.model.wheel.update(view.model.boardObject.getShowWheelPlayer());
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d(tag, "onRetainNonConfigurationInstance");
		RetainedConfig config = new RetainedConfig();
		config.client = client;
		config.lastStatus = lastStatus;
		config.soundPool = view.model.soundPool;
		config.intro = view.model.intro;
		config.connectTask = connectTask;
		this.connectTask = null;
		view.model.soundPool = null;
		// ensure that this isn't disconnect on rotate
		if (client != null) {
			client.removeObserver(this);
			client.removeObserver(view);
		}
		client = null;
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
		if (client.game == null)
			return;
		synchronized (client) {
			Game l = client.game;
			if (!l.isFinished())
				outState.putSerializable("game", l);
		}
	}

	private boolean readStateFromBundle(Bundle in) {
		try {
			Game game = (Game) in.getSerializable("game");
			if (game == null)
				return false;
			// don't restore games that have finished; the server would not detach the listener
			if (game.isFinished())
				return false;

			final Board board = game.getBoard();
			Crashlytics.log("restore from bundle");
			int ret = JNIServer.runServerForExistingGame(game, difficulty);
			if (ret != 0) {
				Crashlytics.log("Error starting server: " + ret);
			}

			/* this will start a new GameClient, which needs to be restored from saved gamestate first */
			final GameConfiguration config = GameConfiguration.builder()
				.difficulty(difficulty)
				.fieldSize(board.width)
				.build();

			client = new GameClient(game, config);

			client.addObserver(this);
			client.addObserver(view);

			client.game.setStarted(true);

			view.setGame(client.game);
			connectTask = new ConnectTask(client, false, () -> {
				// when resuming, the server does not send a current player message
				newCurrentPlayer(game.getCurrentPlayer());
			});
			connectTask.setActivity(this);

			// this call would execute the onPreTask method, which calls through to show the progress
			// dialog. But because performRestoreInstanceState calls restoreManagedDialogs, those
			// dialogs would be overwritten. To mitigate this, we need to defer starting the connectTask
			// until all restore is definitely complete.
			view.post(() -> {
				if (connectTask != null) connectTask.execute((String) null);
			});

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	long gameStartTime = 0;

	public void startNewGame() {
		if (client != null) {
			// when starting a new game from the options menu, keep previous config
			startNewGame(client.getConfig(), null);
		} else {
			// else start default game
			startNewGame(GameConfiguration.builder().build(), null);
		}
	}

	@UiThread
	public void startNewGame(final GameConfiguration config, final Runnable runAfter) {
		newCurrentPlayer(-1);
		if (config.getServer() == null) {
			int ret = JNIServer.runServerForNewGame(
				config.getGameMode(),
				config.getFieldSize(),
				config.getStones(),
				config.getDifficulty()
			);

			if (ret != 0) {
				Crashlytics.log("Error starting server: " + ret);
			}
		}

		if (client != null)
			client.disconnect();
		client = null;

		view.model.clearEffects();

		final Board board = new Board(fieldsize);
		final Game game = new Game(board);
		client = new GameClient(game, config);
		board.startNewGame(config.getGameMode(), config.getFieldSize(), config.getFieldSize());
		board.setAvailableStones(0, 0, 0, 0, 0);

		client.addObserver(this);
		client.addObserver(view);

		view.setGame(client.game);

		connectTask = new ConnectTask(client, config.getShowLobby(), new Runnable() {
			@Override
			public void run() {
				if (config.getRequestPlayers() == null)
					client.requestPlayer(-1, clientName);
				else {
					for (int i = 0; i < 4; i++)
						if (config.getRequestPlayers()[i])
							client.requestPlayer(i, clientName);
				}
				if (!config.getShowLobby())
					client.requestGameStart();
				else {
					Bundle b = new Bundle();
					b.putString("server", config.getServer() == null ? "localhost" : config.getServer());
					FirebaseAnalytics.getInstance(FreebloksActivity.this).logEvent("show_lobby", b);
				}

				if (config.getServer() == null) {
					// hosting a local game. start bluetooth bridge.
					final BluetoothServerThread bluetoothServer = new BluetoothServerThread(
						// start a new client bridge for every connected bluetooth client
						socket -> new BluetoothClientToSocketThread(socket, "localhost", GameClient.DEFAULT_PORT).start()
					);
					client.addObserver(bluetoothServer);
					bluetoothServer.start();
				}

				if (runAfter != null)
					runAfter.run();
			}
		});
		connectTask.setActivity(this);
		connectTask.execute(config.getServer());
	}

	@UiThread
	public void establishBluetoothGame(BluetoothSocket socket) throws IOException {
		final GameConfiguration config = GameConfiguration.builder().build();
		newCurrentPlayer(-1);

		if (client != null)
			client.disconnect();
		client = null;
		view.model.clearEffects();
		Log.d(tag, "Establishing game with existing bluetooth connection");

		final Board board = new Board(fieldsize);
		final Game game = new Game(board);
		board.startNewGame(GameMode.GAMEMODE_4_COLORS_4_PLAYERS);
		board.setAvailableStones(0, 0, 0, 0, 0);

		client = new GameClient(game, config);
		view.setGame(game);

		client.addObserver(this);
		client.addObserver(view);
		client.connected(socket, socket.getInputStream(), socket.getOutputStream());

		if (config.getRequestPlayers() == null)
			client.requestPlayer(-1, clientName);

		showDialog(FreebloksActivity.DIALOG_LOBBY);

		FirebaseAnalytics.getInstance(this).logEvent("bluetooth_connected", null);
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

			byte[] bytes = bos.toByteArray();
			bos.close();

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
		synchronized (client) {
			writeStateToBundle(b);
			p.writeBundle(b);
		}
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
					Crashlytics.logException(e);
					e.printStackTrace();
				}
			}
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
		if (client != null && client.game != null)
			local = client.game.isLocalPlayer();

		menu.findItem(R.id.undo).setEnabled(local && lastStatus != null && lastStatus.getClients() <= 1);
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
		optionsMenu.findItem(R.id.sound_toggle_button).setIcon(on ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_LOBBY:
				if (client == null)
					return null;
				return new LobbyDialog(this, chatEntries);

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
						startNewGame(dialog.getConfiguration(), null);
						dismissDialog(DIALOG_CUSTOM_GAME);
						dismissDialog(DIALOG_GAME_MENU);
						return true;
					}
				});

			case DIALOG_JOIN:
				return new JoinDialog(this, new JoinDialog.OnStartCustomGameListener() {
					@Override
					public void setClientName(String name) {
						clientName = name;
					}

					@Override
					public void onJoinGame(String server) {
						startNewGame(GameConfiguration.builder()
								.server(server)
								.showLobby(true)
								.build(),
							null
						);
						dismissDialog(DIALOG_GAME_MENU);
					}

					@Override
					public void onHostGame() {
						startNewGame(GameConfiguration.builder().showLobby(true).build(), null);
						dismissDialog(DIALOG_GAME_MENU);
					}

					@Override
					public void onHostBluetoothGameWithClient(final BluetoothSocket clientSocket) {
						dismissDialog(DIALOG_GAME_MENU);
						startNewGame(GameConfiguration.builder().showLobby(true).build(), new Runnable() {
							@Override
							public void run() {
								new BluetoothClientToSocketThread(clientSocket, "localhost", GameClient.DEFAULT_PORT).start();
							}
						});
					}

					@Override
					@UiThread
					public void onJoinGame(BluetoothSocket socket) {
						// got a connected bluetooth socket to a server
						dismissDialog(DIALOG_GAME_MENU);

						try {
							establishBluetoothGame(socket);
						} catch (IOException e) {
							e.printStackTrace();
						}
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
					new ColorListDialog.OnColorSelectedListener() {
						@Override
						public void onColorSelected(ColorListDialog dialog, int color) {
							boolean[] players = new boolean[4];
							players[color] = true;
							onColorsSelected(dialog, players);
						}

						@Override
						public void onRandomColorSelected(ColorListDialog dialog) {
							onColorsSelected(dialog, null);
						}

						@Override
						public void onColorsSelected(ColorListDialog dialog, boolean[] players) {
							gamemode = dialog.getGameMode();
							fieldsize = dialog.getBoardSize();
							final GameConfiguration config = GameConfiguration.builder()
								.requestPlayers(players)
								.fieldSize(fieldsize)
								.gameMode(gamemode)
								.showLobby(false)
								.build();
							startNewGame(config, null);

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
					((LobbyDialog) dialog).setGameClient(client);
					if (lastStatus != null)
						((LobbyDialog) dialog).serverStatus(lastStatus);
					dialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							if (!client.game.isStarted() && !client.game.isFinished()) {
								FirebaseAnalytics.getInstance(FreebloksActivity.this).logEvent("lobby_close", null);
								canresume = false;
								client.disconnect();
								client = null;
								showDialog(DIALOG_GAME_MENU);
							}
						}
					});
					if (client.game.isStarted()) {
						dialog.setCanceledOnTouchOutside(true);
					} else {
						dialog.setCanceledOnTouchOutside(false);
					}
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
				GameMenu g = (GameMenu) dialog;
				g.setResumeEnabled(canresume);
				break;

			case DIALOG_JOIN:
				((JoinDialog) dialog).setName(clientName);
				break;

			case DIALOG_CUSTOM_GAME:
				((CustomGameDialog) dialog).prepareCustomGameDialog(difficulty, gamemode, fieldsize);
				break;

			case DIALOG_PROGRESS:
				if (connectTask != null)
					dialog.setOnCancelListener(connectTask);
				break;

			case DIALOG_SINGLE_PLAYER:
				ColorListDialog d = (ColorListDialog) dialog;
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
					if (client == null || (client.game != null && client.game.isFinished()))
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
				editor.apply();
				updateSoundMenuEntry();
				Toast.makeText(this, getString(view.model.soundPool.isEnabled() ? R.string.sound_on : R.string.sound_off), Toast.LENGTH_SHORT).show();
				return true;

			case R.id.hint:
				if (client == null)
					return true;
				findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
				findViewById(R.id.movesLeft).setVisibility(View.INVISIBLE);
				view.model.currentStone.stopDragging();
				client.requestHint();
				return true;

			case R.id.undo:
				if (client == null)
					return true;
				view.model.clearEffects();
				client.requestUndo();
				view.model.soundPool.play(view.model.soundPool.SOUND_UNDO, 1.0f, 1.0f);
				return true;

			case R.id.show_main_menu:
				if (client != null && client.game.isStarted() && lastStatus != null && lastStatus.getClients() > 1)
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
					startNewGame();
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
				if (view == null)
					return;
				if (multiplayerNotification != null)
					updateMultiplayerNotification(false, null);
				boolean local = false;
				int showPlayer = view.model.boardObject.getShowDetailsPlayer();
				if (client != null && client.game != null)
					local = client.game.isLocalPlayer(player);
				else
					showPlayer = player;

				if (optionsMenu != null) {
					optionsMenu.findItem(R.id.hint).setEnabled(local);
					optionsMenu.findItem(R.id.undo).setEnabled(local && lastStatus != null && lastStatus.getClients() <= 1);
				}

				findViewById(R.id.progressBar).setVisibility((local || player < 0) ? View.GONE : View.VISIBLE);

				TextView movesLeft, points, status;
				movesLeft = (TextView) findViewById(R.id.movesLeft);
				movesLeft.setVisibility(View.INVISIBLE);
				points = (TextView) findViewById(R.id.points);
				points.setVisibility(View.INVISIBLE);

				status = (TextView) findViewById(R.id.currentPlayer);
				status.clearAnimation();
				findViewById(R.id.myLocation).setVisibility((showPlayer >= 0) ? View.VISIBLE : View.INVISIBLE);
				if (player < 0)
					statusView.setBackgroundColor(Color.rgb(64, 64, 80));

				final Game game = client == null ? null : client.game;
				final Board board = game == null ? null : game.getBoard();

				if (view.model.intro != null)
					status.setText(R.string.touch_to_skip);
				else if (client == null || !client.isConnected())
					status.setText(R.string.not_connected);
				else if (client.game.isFinished()) {
					int pl = view.model.boardObject.getShowWheelPlayer();
					if (pl >= 0) {
						int res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[view.model.getPlayerColor(pl)];
						Player p = board.getPlayer(pl);
						status.setText("[" + getPlayerName(pl) + "]");
						statusView.setBackgroundColor(getResources().getColor(res));
						points.setVisibility(View.VISIBLE);
						points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.getTotalPoints(), p.getTotalPoints()));
						movesLeft.setVisibility(View.VISIBLE);
						movesLeft.setText(getResources().getQuantityString(R.plurals.number_of_stones_left, p.getStonesLeft(), p.getStonesLeft()));
					}
				} else if (player >= 0 || showPlayer >= 0) {
					if (showPlayer < 0) {
						int res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[view.model.getPlayerColor(player)];
						statusView.setBackgroundColor(getResources().getColor(res));
						Player p = client.game.getBoard().getPlayer(player);
						points.setVisibility(View.VISIBLE);
						points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.getTotalPoints(), p.getTotalPoints()));
						if (!local)
							status.setText(getString(R.string.waiting_for_color, getPlayerName(player)));
						else {
							status.setText(getString(R.string.your_turn, getPlayerName(player)));

							movesLeft.setVisibility(View.VISIBLE);
							movesLeft.setText(getResources().getQuantityString(R.plurals.player_status_moves, p.getNumberOfPossibleTurns(), p.getNumberOfPossibleTurns()));
						}
					} else {
						int res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[view.model.getPlayerColor(showPlayer)];
						statusView.setBackgroundColor(getResources().getColor(res));
						Player p = board.getPlayer(showPlayer);
						points.setVisibility(View.VISIBLE);
						points.setText(getResources().getQuantityString(R.plurals.number_of_points, p.getTotalPoints(), p.getTotalPoints()));

						if (p.getNumberOfPossibleTurns() <= 0)
							status.setText("[" + getString(R.string.color_is_out_of_moves, getPlayerName(showPlayer)) + "]");
						else {
							status.setText(getPlayerName(showPlayer));

							movesLeft.setVisibility((local || player < 0) ? View.VISIBLE : View.INVISIBLE);
							movesLeft.setText(getResources().getQuantityString(R.plurals.player_status_moves, p.getNumberOfPossibleTurns(), p.getNumberOfPossibleTurns()));
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
	public void stoneWillBeSet(@NonNull Turn turn) {
		for (int i = 0; i < 4; i++)
			number_of_possible_turns[i] = client.game.getBoard().getPlayer(i).getNumberOfPossibleTurns();
	}

	@Override
	public void stoneHasBeenSet(@NonNull final Turn turn) {
		if (client == null)
			return;
		if (view == null)
			return;
		final Game game = client.game;
		final Board board = game.getBoard();
		if (game == null)
			return;
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!game.isLocalPlayer(turn.getPlayer())) {
					if (view == null)
						return;
					if (view.model.soundPool == null)
						return;
					view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float) Math.random() * 0.2f);
					vibrate(Global.VIBRATE_SET_STONE);
				}
			}
		});

		for (int i = 0; i < 4; i++) {
			final Player p = board.getPlayer(i);
			if (p.getNumberOfPossibleTurns() <= 0 && number_of_possible_turns[i] > 0) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (view != null) {
							Toast.makeText(FreebloksActivity.this, getString(R.string.color_is_out_of_moves, getPlayerName(p.getNumber())), Toast.LENGTH_SHORT).show();

							if (view.model.soundPool != null)
								view.model.soundPool.play(view.model.soundPool.SOUND_PLAYER_OUT, 0.8f, 1.0f);
							if (view.model.hasAnimations()) {
								int sx, sy;
								sx = board.getPlayerSeedX(p.getNumber(), gamemode);
								sy = board.getPlayerSeedY(p.getNumber(), gamemode);
								for (int x = 0; x < board.width; x++)
									for (int y = 0; y < board.height; y++)
										if (board.getFieldPlayer(y, x) == p.getNumber()) {
											boolean effected = false;
											synchronized (view.model.effects) {
												for (int j = 0; j < view.model.effects.size(); j++)
													if (view.model.effects.get(j).isEffected(x, y)) {
														effected = true;
														break;
													}
											}
											if (!effected) {
												final float distance = (float) Math.sqrt((x - sx) * (x - sx) + (y - sy) * (y - sy));
												Effect effect = new BoardStoneGlowEffect(
													view.model,
													view.model.getPlayerColor(p.getNumber()),
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
	public void hintReceived(@NonNull final Turn turn) {
		FirebaseAnalytics.getInstance(this).logEvent("hint_received", null);

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

		Bundle b = new Bundle();
		b.putString("server", client.getConfig().getServer());
		b.putString("game_mode", client.game.getGameMode().toString());
		b.putInt("w", client.game.getBoard().width);
		b.putInt("h", client.game.getBoard().height);
		b.putInt("clients", lastStatus.getClients());
		b.putInt("players", lastStatus.getPlayer());
		FirebaseAnalytics.getInstance(this).logEvent("game_finished", b);

		/* TODO: play sound on game finish */
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (client == null || client.game == null)
					return;
				updateMultiplayerNotification(false, null);
				PlayerScore[] data = client.game.getPlayerScores();
				new AddScoreTask(getApplicationContext(), client.game.getGameMode()).execute(data);

				if (client.game == null) {
					Crashlytics.logException(new IllegalStateException("gameFinished, but no game running"));
					return;
				}

				Intent intent = new Intent(FreebloksActivity.this, GameFinishActivity.class);
				intent.putExtra("game", (Serializable) client.game);
				intent.putExtra("lastStatus", (Serializable) lastStatus);
				intent.putExtra("clientName", clientName);
				startActivityForResult(intent, REQUEST_FINISH_GAME);
			}
		});
	}

	@Override
	public void chatReceived(int client, @NonNull final String message) {
		String name;
		int player = -1;
		if (lastStatus != null && client >= 0) {
			if (lastStatus.isAtLeastVersion(2))
				for (int i = 0; i < lastStatus.getClientForPlayer().length; i++)
					if (lastStatus.getClient(i) == client) {
						player = i;
						break;
					}
			name = lastStatus.getClientName(getResources(), client);
		} else {
			/* if we have advanced status, ignore all server messages (c == -1) */
			/* server messages are generated in serverStatus */
			if (lastStatus != null && lastStatus.isAtLeastVersion(2))
				return;
			name = getString(R.string.client_d, client + 1);
		}

		final ChatEntry e = new ChatEntry(client, message, name);
		e.setPlayer(player);

		if (!this.client.game.isLocalPlayer(player) &&
			(this.client.game.isStarted() || multiplayerNotification != null))
			updateMultiplayerNotification(true, e.toString());

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				chatEntries.add(e);
				if (client == -1)
					Toast.makeText(FreebloksActivity.this, "* " + message,
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

		Bundle b = new Bundle();
		b.putString("server", client.getConfig().getServer());
		b.putString("game_mode", client.game.getGameMode().toString());
		b.putInt("w", client.game.getBoard().width);
		b.putInt("h", client.game.getBoard().height);
		b.putInt("clients", lastStatus.getClients());
		b.putInt("players", lastStatus.getPlayer());

		FirebaseAnalytics.getInstance(this).logEvent("game_started", b);

		if (lastStatus.getClients() >= 2) {
			FirebaseAnalytics.getInstance(this).logEvent("game_start_multiplayer", b);
		}

		Log.d(tag, "Game started");
		for (int i = 0; i < Board.PLAYER_MAX; i++)
			if (client.game.isLocalPlayer(i))
				Log.d(tag, "Local player: " + i);
	}

	@Override
	public void stoneUndone(@NonNull Turn t) {
		FirebaseAnalytics.getInstance(this).logEvent("undo_move", null);
	}

	@Override
	public void serverStatus(@NonNull MessageServerStatus status) {
		if (lastStatus != null && lastStatus.isAtLeastVersion(2)) {
			/* generate server chat messages, aka "joined" and "left" */

			for (int i = 0; i < lastStatus.getClientForPlayer().length; i++) {
				MessageServerStatus s;
				final boolean wasClient = lastStatus.isClient(i);
				final boolean isClient = status.isClient(i);
				final int tid;
				if (!wasClient && isClient) {
					/* joined */
					s = status;
					tid = R.string.player_joined_color;
				} else if (wasClient && !isClient) {
					/* left */
					s = lastStatus;
					tid = R.string.player_left_color;
				} else continue;
				String name;
				name = s.getClientName(getResources(), s.getClientForPlayer()[i]);

				if (view == null)
					return;
				if (view.model.game == null)
					return;

				final String text = getString(tid, name, getResources().getStringArray(R.array.color_names)[view.model.getPlayerColor(i)]);
				final ChatEntry e = new ChatEntry(-1, text, name);
				e.setPlayer(i);

				if (!view.model.game.isLocalPlayer(i))
					updateMultiplayerNotification(tid == R.string.player_left_color && client.game.isStarted(), text);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						chatEntries.add(e);
					}
				});
			}
		}
		lastStatus = status;
		if (lastStatus.getClients() > 1) {
			chatButton.post(new Runnable() {
				@Override
				public void run() {
					chatButton.setVisibility(View.VISIBLE);
				}
			});
		}
	}

	@Override
	public void onConnected(@NonNull GameClient client) {
		newCurrentPlayer(client.game.getCurrentPlayer());
	}

	@Override
	public void onDisconnected(@NonNull GameClient client, @Nullable Exception error) {
		Log.w(tag, "onDisconnected()");

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
	public boolean commitCurrentStone(@NonNull final Turn turn) {
		if (client == null)
			return false;

		if (!client.game.isLocalPlayer())
			return false;
		if (!client.game.getBoard().isValidTurn(turn))
			return false;

		if (view.model.hasAnimations()) {
			StoneRollEffect e = new StoneRollEffect(view.model, turn, view.model.currentStone.hover_height_high, -15.0f);

			EffectSet set = new EffectSet();
			set.add(e);
			set.add(new StoneFadeEffect(view.model, turn, 1.0f));
			view.model.addEffect(set);
		}

		view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float) Math.random() * 0.2f);
		vibrate(Global.VIBRATE_SET_STONE);

		client.setStone(turn);
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

			client.requestUndo();

			view.model.soundPool.play(view.model.soundPool.SOUND_UNDO, 1.0f, 1.0f);
			return;
		}
		if (client != null && client.game.isStarted() && !client.game.isFinished() && lastStatus != null && lastStatus.getClients() > 1)
			showDialog(DIALOG_QUIT);
		else {
			if (view.model.intro != null) {
				view.model.intro.cancel();
			} else {
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
		if (client.game == null)
			return;
		newCurrentPlayer(client.game.getCurrentPlayer());
	}

	String getPlayerName(int player) {
		String color_name = getResources().getStringArray(R.array.color_names)[view.model.getPlayerColor(player)];
		/* this will ensure that always the local name is used, even though the server
		 * might still have stored an old or no name at all
		 *
		 * When resuming a game, the name is lost and never set again. This is a non issue now.
		 */
		if (clientName != null && clientName.length() > 0 && client != null && client.game != null && client.game.isLocalPlayer(player))
			return clientName;
		if (lastStatus == null)
			return color_name;
		return lastStatus.getPlayerName(getResources(), player, view.model.getPlayerColor(player));
	}

	Notification.Builder notificationBuilder;

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannels() {
		NotificationChannel channel = new NotificationChannel("default", getString(R.string.notification_channel_default), NotificationManager.IMPORTANCE_DEFAULT);
		channel.enableVibration(true);
		channel.enableLights(true);
		notificationManager.createNotificationChannel(channel);
	}

	void updateMultiplayerNotification(boolean forceShow, String chat) {
		if (client == null || client.game == null)
			return;
		if (!client.isConnected())
			return;
		if (multiplayerNotification == null && !forceShow)
			return;
		if (!show_notifications)
			return;
		if (client == null)
			return;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannels();
		}

		Intent intent = new Intent(this, FreebloksActivity.class);
		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		if (chat != null)
			intent.putExtra("showChat", true);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (notificationBuilder == null) {
			notificationBuilder = new Notification.Builder(this);

			notificationBuilder.setContentIntent(pendingIntent);

			notificationBuilder.addAction(android.R.drawable.ic_media_play, getString(R.string.notification_continue), pendingIntent);

			intent = new Intent(this, FreebloksActivity.class);
			intent.setAction(Intent.ACTION_DELETE);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("disconnect", true);
			PendingIntent disconnectIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			notificationBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.notification_disconnect), disconnectIntent);
		}

		notificationBuilder.setContentTitle(getString(R.string.app_name))
			.setOngoing(false)
			.setDefaults(0)
			.setTicker(null)
			.setAutoCancel(true)
			.setSound(null);

		notificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);

		if ((forceShow && chat == null) || multiplayerNotification != null)
			notificationBuilder.setOngoing(true);

		if (!client.game.isStarted()) {
			notificationBuilder.setSmallIcon(R.drawable.notification_waiting_small);
			notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.appicon_small));
			notificationBuilder.setContentText(getString(R.string.lobby_waiting_for_players));
			notificationBuilder.setTicker(getString(R.string.lobby_waiting_for_players));
		} else if (client.game.isFinished()) {
			notificationBuilder.setSmallIcon(R.drawable.notification_your_turn);
			notificationBuilder.setContentText(getString(R.string.game_finished));
			notificationBuilder.setOngoing(false);
		} else {
			if (client.game.getCurrentPlayer() < 0)
				return;
			if (client.game.isLocalPlayer()) {
				notificationBuilder.setSmallIcon(R.drawable.notification_your_turn);
				notificationBuilder.setContentText(getString(R.string.your_turn, getPlayerName(client.game.getCurrentPlayer())));
				notificationBuilder.setTicker(getString(R.string.your_turn, getPlayerName(client.game.getCurrentPlayer())));

				if (!forceShow) {
					notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

					notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
				}
			} else {
				notificationBuilder.setSmallIcon(R.drawable.notification_waiting_small);
				notificationBuilder.setContentText(getString(R.string.waiting_for_color, getPlayerName(client.game.getCurrentPlayer())));
				notificationBuilder.setTicker(getString(R.string.waiting_for_color, getPlayerName(client.game.getCurrentPlayer())));
			}
		}

		if (chat != null) {
			notificationBuilder.setSmallIcon(R.drawable.notification_chat);
			notificationBuilder.setContentText(chat);
			notificationBuilder.setTicker(chat);
			notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

			notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
			notificationBuilder.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.chat));
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notificationBuilder.setChannelId("default");
		}

		Notification n;

		n = notificationBuilder.build();

		if (chat == null)
			multiplayerNotification = n;

		notificationManager.notify(NOTIFICATION_GAME_ID, n);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (Intent.ACTION_DELETE.equals(intent.getAction())) {
			Log.d(tag, "ACTION_DELETE");
			finish();
			return;
		} else {
			if (intent.hasExtra("showChat") && client != null && client.game.isStarted())
				showDialog(DIALOG_LOBBY);
		}
		super.onNewIntent(intent);
	}

	@Override
	public void onSignInFailed() {

	}

	@Override
	public void onSignInSucceeded() {
		if (Global.IS_VIP) {
			unlock(getString(R.string.achievement_vip));
		}
	}
}
