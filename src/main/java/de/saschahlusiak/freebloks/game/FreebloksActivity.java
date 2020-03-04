package de.saschahlusiak.freebloks.game;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;

import de.saschahlusiak.freebloks.BuildConfig;
import de.saschahlusiak.freebloks.Global;
import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.client.GameClient;
import de.saschahlusiak.freebloks.client.GameEventObserver;
import de.saschahlusiak.freebloks.client.JNIServer;
import de.saschahlusiak.freebloks.donate.DonateActivity;
import de.saschahlusiak.freebloks.lobby.LobbyDialog;
import de.saschahlusiak.freebloks.model.Board;
import de.saschahlusiak.freebloks.model.Game;
import de.saschahlusiak.freebloks.model.GameConfig;
import de.saschahlusiak.freebloks.model.GameMode;
import de.saschahlusiak.freebloks.model.Player;
import de.saschahlusiak.freebloks.model.PlayerScore;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.message.MessageServerStatus;
import de.saschahlusiak.freebloks.preferences.FreebloksPreferences;
import de.saschahlusiak.freebloks.view.Freebloks3DView;
import de.saschahlusiak.freebloks.view.effects.BoardStoneGlowEffect;
import de.saschahlusiak.freebloks.view.effects.Effect;
import de.saschahlusiak.freebloks.view.scene.Intro;
import de.saschahlusiak.freebloks.view.scene.Scene;
import de.saschahlusiak.freebloks.view.scene.Theme;
import io.fabric.sdk.android.Fabric;

public class FreebloksActivity extends BaseGameActivity implements GameEventObserver, Intro.OnIntroCompleteListener, OnStartCustomGameListener {
	static final String tag = FreebloksActivity.class.getSimpleName();

	static final int DIALOG_GAME_MENU = 1;
	static final int DIALOG_LOBBY = 2;
	static final int DIALOG_QUIT = 3;
	static final int DIALOG_RATE_ME = 4;
	static final int DIALOG_JOIN = 5;
	static final int DIALOG_NEW_GAME_CONFIRMATION = 8;
	static final int DIALOG_SINGLE_PLAYER = 10;

	static final int REQUEST_FINISH_GAME = 1;

	private static final String GAME_STATE_FILE = "gamestate.bin";

	private SharedPreferences prefs;
	private Freebloks3DView view;
	private GameClient client = null;
	private boolean undo_with_back;
	private MessageServerStatus lastStatus;
	private Menu optionsMenu;
	private ImageButton chatButton;
	private boolean showRateDialog = false;

	private FreebloksActivityViewModel viewModel;

	@Deprecated private String clientName;

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

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		getActionBar().setBackgroundDrawable(new ColorDrawable(0));
		getActionBar().setDisplayShowTitleEnabled(false);

		setContentView(R.layout.main_3d);

		prefs = PreferenceManager.getDefaultSharedPreferences(FreebloksActivity.this);

		viewModel = new ViewModelProvider(this).get(FreebloksActivityViewModel.class);

		view = findViewById(R.id.board);
		view.setActivity(viewModel);

		if (prefs.getBoolean("immersive_mode", true)) {
			view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		chatButton = findViewById(R.id.chatButton);
		chatButton.setVisibility(View.INVISIBLE);
		chatButton.setOnClickListener(v -> {
			chatButton.clearAnimation();
			showDialog(DIALOG_LOBBY);
		});

		client = viewModel.getClient();
		lastStatus = viewModel.getLastStatus();
		if (viewModel.getIntro() != null)
			viewModel.getIntro().setModel(view.model, this);

		chatButton.setVisibility((lastStatus != null && lastStatus.getClients() > 1) ? View.VISIBLE : View.INVISIBLE);

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

			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.bottomSheet, new PlayerDetailFragment())
				.commit();
		}

		if (view.model.soundPool == null)
			view.model.soundPool = viewModel.getSounds();

		clientName = prefs.getString("player_name", null);

		if (client != null) {
			/* we just rotated and got *hot* objects */
			setGameClient(client);
		} else if (savedInstanceState == null) {
			if (prefs.getBoolean("show_animations", true) && !prefs.getBoolean("skip_intro", false)) {
				viewModel.setIntro(new Intro(getApplicationContext(), view.model, this));
			} else
				OnIntroCompleted();
		}

		findViewById(R.id.myLocation).setOnClickListener(v -> view.model.boardObject.resetRotation());

		viewModel.getConnectionStatusLiveData().observe(this, this::onConnectionStatusChanged);
		viewModel.getPlayerToShowInSheet().observe(this, this::playerSheetChanged);
		viewModel.getSoundsEnabledLiveData().observe(this, this::soundEnabledChanged);
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(tag, "onRestoreInstanceState (bundle=" + savedInstanceState + ")");

		if (client == null) {
			readStateFromBundle(savedInstanceState);
		}
	}

	public FreebloksActivityViewModel getViewModel() {
		return viewModel;
	}

	private void onConnectionStatusChanged(@NonNull ConnectionStatus status) {
		Log.d(tag, "Connection status: " + status);
		final String tag = "connecting_progress_dialog";
		final DialogFragment f = (DialogFragment) getSupportFragmentManager().findFragmentByTag(tag);

		switch (status) {
			case Connecting:
				if (f == null) {
					new ConnectingDialogFragment().show(getSupportFragmentManager(), tag);
					// there seems to be a race condition where disconnecting happens before the dialog is done showing,
					// so it fails to be dismissed later. So we force executing the above transaction.
					getSupportFragmentManager().executePendingTransactions();
				}
				break;

			case Connected:
			case Failed:
			case Disconnected:
				if (f != null) {
					f.dismiss();
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void OnIntroCompleted() {
		viewModel.setIntro(null);
		viewModel.setSheetPlayer(-1, false);
		try {
			restoreOldGame();
		} catch (Exception e) {
			Toast.makeText(FreebloksActivity.this, R.string.could_not_restore_game, Toast.LENGTH_LONG).show();
		}

		final boolean canResume = (client != null && client.game.isStarted() && !client.game.isFinished());

		if (!canResume || !prefs.getBoolean("auto_resume", false))
			showDialog(DIALOG_GAME_MENU);

		if (showRateDialog)
			showDialog(DIALOG_RATE_ME);
	}

	@Override
	protected void onDestroy() {
		Log.d(tag, "onDestroy");

		if (client != null) {
			client.removeObserver(this);
			client.removeObserver(view);
		}
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
		viewModel.onStop();
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

		viewModel.reloadPreferences();
		view.model.showSeeds = prefs.getBoolean("show_seeds", true);
		view.model.showOpponents = prefs.getBoolean("show_opponents", true);
		view.model.showAnimations = Integer.parseInt(prefs.getString("animations", String.format("%d", Scene.ANIMATIONS_FULL)));
		view.model.snapAid = prefs.getBoolean("snap_aid", true);
		view.model.immersiveMode = prefs.getBoolean("immersive_mode", true);
		undo_with_back = prefs.getBoolean("back_undo", false);
		clientName = prefs.getString("player_name", null);
		if (clientName != null && clientName.equals(""))
			clientName = null;
		final Theme t = Theme.get(this, prefs.getString("theme", "texture_wood"), false);
		view.setTheme(t);

		viewModel.onStart();

		/* update wheel in case showOpponents has changed */
		view.model.wheel.update(view.model.boardObject.getShowWheelPlayer());
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		Log.d(tag, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		outState.putFloat("view_scale", view.getScale());
		outState.putBoolean("showRateDialog", showRateDialog);
		writeStateToBundle(outState);
	}

	private void writeStateToBundle(Bundle outState) {
		if (client == null)
			return;
		synchronized (client) {
			Game l = client.game;
			if (!l.isFinished())
				outState.putSerializable("game", l);
		}
	}

	private boolean readStateFromBundle(Bundle in) {
		try {
			final Game game = (Game) in.getSerializable("game");
			if (game == null)
				return false;

			// don't restore games that have finished; the server would not detach the listener
			if (game.isFinished())
				return false;

			Crashlytics.log("Resuming game from bundle");
			Log.d(tag, "Resuming game from bundle");
			resumeGame(game);

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
			startNewGame(new GameConfig(), null);
		}
	}

	private void setGameClient(final @NonNull GameClient client) {
		this.client = client;

		client.addObserver(this);
		client.addObserver(view);

		viewModel.setClient(client);

		view.setGameClient(client);
	}

	@UiThread
	private void resumeGame(@NonNull final Game game) {
		final GameMode gameMode = game.getGameMode();

		final int previousDifficulty = prefs.getInt("difficulty", GameConfig.DEFAULT_DIFFICULTY);
		int ret = JNIServer.runServerForExistingGame(game, previousDifficulty);
		if (ret != 0) {
			Crashlytics.log("Error starting server: " + ret);
		}

		final GameConfig config = new GameConfig(
			null,
			gameMode,
			false,
			new boolean[] {false, false, false, false},
			previousDifficulty,
			GameConfig.defaultStonesForMode(gameMode),
			game.getBoard().width
		);
		game.setStarted(true);

		// this will start a new GameClient for the saved game state
		setGameClient(new GameClient(game, config));

		viewModel.startConnectingClient(config, clientName, false, () -> { });
	}

	@UiThread
	private void startNewGame(final GameConfig config, final Runnable onConnected) {
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

		viewModel.disconnectClient();

		final Board board = new Board();
		final Game game = new Game(board);
		board.startNewGame(config.getGameMode(), config.getFieldSize(), config.getFieldSize());

		setGameClient(new GameClient(game, config));

		viewModel.startConnectingClient(config, clientName, !config.getShowLobby(), onConnected);
	}

	private void restoreOldGame() throws Exception {
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
			} else {
			}
		} catch (FileNotFoundException fe) {
			/* signal non-failure if game state file is missing */
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
		if (client != null)
			local = client.game.isLocalPlayer();

		menu.findItem(R.id.undo).setEnabled(local && lastStatus != null && lastStatus.getClients() <= 1);
		menu.findItem(R.id.hint).setEnabled(local);
		menu.findItem(R.id.sound_toggle_button).setVisible(true);
		soundEnabledChanged(viewModel.getSoundsEnabled());

		return super.onPrepareOptionsMenu(menu);
	}

	private void soundEnabledChanged(boolean enabled) {
		if (optionsMenu == null)
			return;

		optionsMenu.findItem(R.id.sound_toggle_button).setTitle(enabled ? R.string.sound_on : R.string.sound_off);
		optionsMenu.findItem(R.id.sound_toggle_button).setIcon(enabled ? R.drawable.ic_volume_up_white_48dp : R.drawable.ic_volume_off_white_48dp);
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_LOBBY:
				if (client == null)
					return null;
				return new LobbyDialog(this);

			case DIALOG_QUIT:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.do_you_want_to_leave_current_game);
				builder.setPositiveButton(android.R.string.yes, (dialog, which) -> showDialog(DIALOG_GAME_MENU));
				builder.setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss());
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

			case DIALOG_JOIN:
				return new JoinDialog(this, this);

			case DIALOG_SINGLE_PLAYER:
				final ColorListDialog d = new ColorListDialog(this,
					(dialog, config) -> {
						startNewGame(config, null);

						dialog.dismiss();
					});
				d.setOnCancelListener(dialog -> showDialog(DIALOG_GAME_MENU));

				return d;

			default:
				return super.onCreateDialog(id);
		}
	}

	@Override
	public void onStartClientGameWithConfig(@NonNull GameConfig config, @Nullable Runnable onConnected) {
		dismissDialog(DIALOG_GAME_MENU);

		startNewGame(config, onConnected);
	}

	@Override
	public void setClientName(@NonNull String name) {
		clientName = name;
	}

	@Override
	public void onConnectToBluetoothDevice(@NotNull BluetoothDevice device) {
		dismissDialog(DIALOG_GAME_MENU);

		final GameConfig config = new GameConfig(null, true);

		viewModel.disconnectClient();

		final Board board = new Board();
		final Game game = new Game();
		board.startNewGame(config.getGameMode(), config.getFieldSize(), config.getFieldSize());

		setGameClient(new GameClient(game, config));

		viewModel.startConnectingBluetooth(device, clientName);
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog, Bundle args) {
		switch (id) {
			case DIALOG_LOBBY:
				if (client != null) {

					dialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							if (!client.game.isStarted() && !client.game.isFinished()) {
								FirebaseAnalytics.getInstance(FreebloksActivity.this).logEvent("lobby_close", null);
								client.disconnect();
								client = null;
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
					showDialog(DIALOG_GAME_MENU);
				}
				break;

			case DIALOG_GAME_MENU:
				final boolean canResume = client != null && client.isConnected() && !client.game.isFinished();
				final GameMenu g = (GameMenu) dialog;
				g.setResumeEnabled(canResume);
				break;

			case DIALOG_JOIN:
				((JoinDialog) dialog).setName(clientName);
				break;
		}
		super.onPrepareDialog(id, dialog, args);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case android.R.id.home:
				showDialog(DIALOG_GAME_MENU);
				if (viewModel.getIntro() != null)
					viewModel.getIntro().cancel();
				return true;

			case R.id.new_game:
				if (viewModel.getIntro() != null)
					viewModel.getIntro().cancel();
				else {
					if (client == null || client.game.isFinished())
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
				boolean enabled = !viewModel.getSoundsEnabled();
				viewModel.setSoundsEnabled(enabled);
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
					showDialog(DIALOG_GAME_MENU);
					if (viewModel.getIntro() != null)
						viewModel.getIntro().cancel();
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

	/**
	 * @param data the data to show
	 */
	@UiThread
	private void playerSheetChanged(final SheetPlayer data) {
		if (view == null)
			return;

		final View myLocation = findViewById(R.id.myLocation);

		if (data.isRotated() && client != null && !client.game.isFinished()) {
			myLocation.setVisibility(View.VISIBLE);
		} else {
			myLocation.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void newCurrentPlayer(final int player) {
		runOnUiThread(() -> {
			boolean isLocalPlayer = (client != null) && client.game.isLocalPlayer(player);

			if (optionsMenu != null) {
				optionsMenu.findItem(R.id.hint).setEnabled(isLocalPlayer);
				// undo only if we are the only client
				optionsMenu.findItem(R.id.undo).setEnabled(isLocalPlayer && lastStatus != null && lastStatus.getClients() <= 1);
			}
		});
	}

	/* we have to store the number of possible turns before and after a stone has been set
	 * to detect blocking of other players */
	private int[] number_of_possible_turns = new int[4];

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

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!game.isLocalPlayer(turn.getPlayer())) {
					if (view == null)
						return;
					if (view.model.soundPool == null)
						return;
					view.model.soundPool.play(view.model.soundPool.SOUND_CLICK1, 1.0f, 0.9f + (float) Math.random() * 0.2f);
					viewModel.vibrate(Global.VIBRATE_SET_STONE);
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
								final GameMode gameMode = game.getGameMode();
								sx = board.getPlayerSeedX(p.getNumber(), gameMode);
								sy = board.getPlayerSeedY(p.getNumber(), gameMode);
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
				if (client == null)
					return;
				final PlayerScore[] data = client.game.getPlayerScores();

				new AddScoreTask(getApplicationContext(), client.game.getGameMode(), data).start();

				Intent intent = new Intent(FreebloksActivity.this, GameFinishActivity.class);
				intent.putExtra("game", (Serializable) client.game);
				intent.putExtra("lastStatus", (Serializable) lastStatus);
				intent.putExtra("clientName", clientName);
				startActivityForResult(intent, REQUEST_FINISH_GAME);
			}
		});
	}

	@Override
	public void chatReceived(@NonNull MessageServerStatus status, int client, int player, @NonNull final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (hasWindowFocus()) {
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
	public void playerJoined(int client, int player, String name) {

	}

	@Override
	public void playerLeft(int client, int player, String name) {

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

	@WorkerThread
	@Override
	public void onConnected(@NonNull GameClient client) {
		if (client.getConfig().getShowLobby()) {
			runOnUiThread(() -> {
				final Bundle bundle = new Bundle();
				final String server = client.getConfig().getServer() == null ? "localhost" : client.getConfig().getServer();
				bundle.putString("server", server);
				FirebaseAnalytics.getInstance(this).logEvent("show_lobby", bundle);

				showDialog(FreebloksActivity.DIALOG_LOBBY);
			});
		}
	}

	@WorkerThread
	@Override
	public void onConnectionFailed(@NotNull GameClient client, @NotNull Exception error) {
		runOnUiThread(() -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(android.R.string.dialog_alert_title);
			builder.setMessage(error.getMessage());
			builder.setIcon(android.R.drawable.ic_dialog_alert);

			builder.setOnDismissListener(dialog -> showDialog(DIALOG_GAME_MENU));
			builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());

			builder.create().show();
		});
	}

	@WorkerThread
	@Override
	public void onDisconnected(@NonNull GameClient client, @Nullable Exception error) {
		Log.w(tag, "onDisconnected()");

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lastStatus = null;
				view.setGameClient(null);
				chatButton.setVisibility(View.INVISIBLE);

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
								restoreOldGame();
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
			if (viewModel.getIntro() != null) {
				viewModel.getIntro().cancel();
			} else {
				showDialog(DIALOG_GAME_MENU);
			}
		}
	}

	private String getPlayerName(int player) {
		final GameMode gameMode = (client == null) ? GameMode.GAMEMODE_4_COLORS_4_PLAYERS : client.game.getGameMode();

		final String colorName = Global.getColorName(this, player, gameMode);

		/* this will ensure that always the local name is used, even though the server
		 * might still have stored an old or no name at all
		 *
		 * When resuming a game, the name is lost and never set again. This is a non issue now.
		 */
		if (clientName != null && clientName.length() > 0 && client != null && client.game.isLocalPlayer(player))
			return clientName;

		if (lastStatus == null)
			return colorName;

		final String playerName = lastStatus.getPlayerName(player);
		if (playerName == null) {
			return colorName;
		} else {
			return playerName;
		}
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
