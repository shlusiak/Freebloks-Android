package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.os.Bundle
import android.os.Parcel
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.saschahlusiak.freebloks.BuildConfig
import de.saschahlusiak.freebloks.DependencyProvider
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.client.JNIServer.runServerForExistingGame
import de.saschahlusiak.freebloks.client.JNIServer.runServerForNewGame
import de.saschahlusiak.freebloks.donate.DonateActivity
import de.saschahlusiak.freebloks.game.dialogs.ConnectingDialog
import de.saschahlusiak.freebloks.game.dialogs.RateAppDialog
import de.saschahlusiak.freebloks.game.dialogs.RateAppDialog.Companion.checkShowRateDialog
import de.saschahlusiak.freebloks.game.finish.GameFinishFragment
import de.saschahlusiak.freebloks.game.lobby.LobbyDialog
import de.saschahlusiak.freebloks.game.lobby.LobbyDialog.LobbyDialogListener
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameConfig.Companion.defaultStonesForMode
import de.saschahlusiak.freebloks.model.Turn
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.ThemeManager.Companion.get
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.effects.BoardStoneGlowEffect
import de.saschahlusiak.freebloks.view.effects.Effect
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import de.saschahlusiak.freebloks.view.scene.intro.IntroCompletedListener
import kotlinx.android.synthetic.main.main_3d.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.lang.Runnable

class FreebloksActivity() : AppCompatActivity(), GameEventObserver, IntroCompletedListener, OnStartCustomGameListener, LobbyDialogListener {
    private lateinit var view: Freebloks3DView
    private var client: GameClient? = null
    private var undoWithBack = false
    private var lastStatus: MessageServerStatus? = null
    private var optionsMenu: Menu? = null
    private var showRateDialog = false
    private val analytics by lazy { DependencyProvider.analytics() }
    val viewModel: FreebloksActivityViewModel by lazy { ViewModelProvider(this).get(FreebloksActivityViewModel::class.java) }
    private lateinit var scene: Scene
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }


    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tag, "onCreate")

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(ThreadPolicy.Builder()
                .detectCustomSlowCalls()
                .detectNetwork()
                .penaltyDeath()
                .build())

            StrictMode.setVmPolicy(VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
//			    .penaltyDeath()
                .build())
        }

        // must initialise before anything else
        DependencyProvider.initialise(this)

        Log.d(tag, "nativeLibraryDir=" + applicationInfo.nativeLibraryDir)

        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)

        super.onCreate(savedInstanceState)

        supportActionBar?.run {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(0))
            setDisplayShowTitleEnabled(false)
        }

        setContentView(R.layout.main_3d)

        view = findViewById(R.id.board)
        scene = Scene(view, viewModel)
        view.setScene(scene)
        volumeControlStream = AudioManager.STREAM_MUSIC
        chatButton.setVisibility(View.INVISIBLE)
        chatButton.setOnClickListener(View.OnClickListener { v: View? ->
            chatButton.clearAnimation()
            LobbyDialog().show(getSupportFragmentManager(), null)
        })
        client = viewModel.client
        val client = client
        lastStatus = viewModel.lastStatus
        viewModel.intro?.listener = this
        chatButton.visibility = if ((lastStatus?.clients ?: 0) > 1) View.VISIBLE else View.INVISIBLE

        if (savedInstanceState != null) {
            view.setScale(savedInstanceState.getFloat("view_scale", 1.0f))
            showRateDialog = savedInstanceState.getBoolean("showRateDialog", false)
        } else {
            view.setScale(prefs.getFloat("view_scale", 1.0f))
            showRateDialog = checkShowRateDialog(this)
            val starts = prefs.getLong("rate_number_of_starts", 0)
            if (!Global.IS_VIP && starts == Global.DONATE_STARTS.toLong()) {
                val intent = Intent(this, DonateActivity::class.java)
                startActivity(intent)
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.bottomSheet, PlayerDetailFragment())
                .commit()
        }

        if (client != null) {
            /* we just rotated and got *hot* objects */
            setGameClient(client)
        } else if (savedInstanceState == null) {
            if (prefs.getBoolean("show_animations", true) && !prefs.getBoolean("skip_intro", false)) {
                viewModel.intro = Intro(applicationContext, scene, this)
            } else onIntroCompleted()
        }

        myLocation.setOnClickListener { scene.boardObject.resetRotation() }
        viewModel.connectionStatus.observe(this) { onConnectionStatusChanged(it) }
        viewModel.playerToShowInSheet.observe(this) { playerSheetChanged(it) }
        viewModel.soundsEnabledLiveData.observe(this) { soundEnabledChanged(it) }
        viewModel.googleAccountSignedIn.observe(this) { signedIn: Boolean? ->
            viewModel.gameHelper.setWindowForPopups(window)
            if (Global.IS_VIP) {
                viewModel.gameHelper.unlock(getString(R.string.achievement_vip))
            }
        }
        viewModel.canRequestHint.observe(this) {
            if (optionsMenu != null) {
                invalidateOptionsMenu()
            }
        }
        viewModel.canRequestUndo.observe(this) {
            if (optionsMenu != null) {
                invalidateOptionsMenu()
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(tag, "onRestoreInstanceState (bundle=$savedInstanceState)")
        if (client == null) {
            readStateFromBundle(savedInstanceState)
        }
    }

    private fun onConnectionStatusChanged(status: ConnectionStatus) {
        Log.d(tag, "Connection status: $status")
        val tag = "connecting_progress_dialog"

        val f = supportFragmentManager.findFragmentByTag(tag) as DialogFragment?
        when (status) {
            ConnectionStatus.Connecting -> {
                if (f == null) {
                    ConnectingDialog().show(supportFragmentManager, tag)
                    // there seems to be a race condition where disconnecting happens before the dialog is done showing,
                    // so it fails to be dismissed later. So we force executing the above transaction.
                    supportFragmentManager.executePendingTransactions()
                }
                chatButton.visibility = View.INVISIBLE
            }
            ConnectionStatus.Connected, ConnectionStatus.Failed, ConnectionStatus.Disconnected -> f?.dismiss()
            else -> {
            }
        }
    }

    override fun onIntroCompleted() {
        viewModel.intro = null
        viewModel.setSheetPlayer(-1, false)
        try {
            restoreOldGame()
        } catch (e: Exception) {
            Toast.makeText(this@FreebloksActivity, R.string.could_not_restore_game, Toast.LENGTH_LONG).show()
        }
        val client = client
        val canResume = ((client != null) && client.game.isStarted && !client.game.isFinished)
        if (!canResume || !prefs.getBoolean("auto_resume", false)) showMainMenu()
        if (showRateDialog) RateAppDialog().show(supportFragmentManager, null)
        view.requestRender()
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        client?.removeObserver(this)
        client?.removeObserver((view))
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(tag, "onPause")
        val client = client
        if ((client != null) && client.game.isStarted && !client.game.isFinished) saveGameState(GAME_STATE_FILE)
        super.onPause()
    }

    override fun onResume() {
        Log.d(tag, "onResume")
        super.onResume()
    }

    override fun onStop() {
        viewModel.onStop()
        view.onPause()
        val editor = prefs.edit()
        editor.putFloat("view_scale", view.getScale())
        editor.apply()
        Log.d(tag, "onStop")
        super.onStop()
    }

    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()
        view.onResume()
        viewModel.reloadPreferences()

        scene.showSeeds = prefs.getBoolean("show_seeds", true)
        scene.showOpponents = prefs.getBoolean("show_opponents", true)
        scene.showAnimations = prefs.getString("animations", Scene.ANIMATIONS_FULL.toString())?.toInt() ?: 0
        scene.snapAid = prefs.getBoolean("snap_aid", true)
        undoWithBack = prefs.getBoolean("back_undo", false)

        val t = get(this).getTheme(prefs.getString("theme", "texture_wood"), ColorThemes.Blue) ?: ColorThemes.Blue
        view.setTheme(t)
        viewModel.onStart()

        /* update wheel in case showOpponents has changed */scene.wheel.update(scene.boardObject.showWheelPlayer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(tag, "onSaveInstanceState")
        super.onSaveInstanceState(outState)
        outState.putFloat("view_scale", view.getScale())
        outState.putBoolean("showRateDialog", showRateDialog)
        writeStateToBundle(outState)
    }

    private fun writeStateToBundle(outState: Bundle) {
        val client = client ?: return
        synchronized(client) {
            val game = client.game
            if (!game.isFinished && game.isStarted) outState.putSerializable("game", game)
        }
    }

    private fun readStateFromBundle(input: Bundle): Boolean {
        try {
            val game = input.getSerializable("game") as Game? ?: return false

            // don't restore games that have finished; the server would not detach the listener
            if (game.isFinished) return false
            DependencyProvider.crashReporter().log("Resuming game from bundle")
            Log.d(tag, "Resuming game from bundle")
            resumeGame(game)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    var gameStartTime: Long = 0

    /**
     * Either starts a game with exactly the last config or a new default game.
     *
     * Called e.g. during long-press, "start new game" in the finish dialog, or on initial startup.
     */
    override fun startNewDefaultGame() {
        val client = client
        if (client != null) {
            // when starting a new game from the options menu, keep previous config
            startNewGame(client.config, viewModel.localClientNameOverride, null)
        } else {
            // else start default game
            startNewGame(GameConfig(), null, null)
        }
    }

    private fun setGameClient(client: GameClient) {
        this.client = client
        client.addObserver(this)
        client.addObserver((view))
        viewModel.setClient(client)
        view.setGameClient(client)
    }

    @UiThread
    private fun resumeGame(game: Game) {
        val gameMode = game.gameMode

        // Unfortunately the JNI portion does not recognise the turn history, even though we have persisted it in the
        // bundle. As such, to avoid minor inconsistencies, we clear the history to be in sync with the JNI portion.
        game.history.clear()
        val previousDifficulty = prefs.getInt("difficulty", GameConfig.DEFAULT_DIFFICULTY)
        val ret = runServerForExistingGame(game, previousDifficulty)
        if (ret != 0) {
            DependencyProvider.crashReporter().log("Error starting server: $ret")
        }
        val config = GameConfig(
            null,
            gameMode,
            false, booleanArrayOf(false, false, false, false),
            previousDifficulty,
            defaultStonesForMode(gameMode),
            game.board.width
        )
        game.isStarted = true

        // this will start a new GameClient for the saved game state
        setGameClient(GameClient(game, config))

        // even though we don't show the lobby, we also don't want to request game start,
        // because it is already running. Also, because we do not request any players,
        // we do not need to pass in a clientName.

        // unfortunately we have lost all player names from before, but this shouldn't matter
        // as the local client name should overwrite what the server believes anyway, and
        // all other players are computers when resuming.
        viewModel.startConnectingClient(config, null, null)
    }

    @UiThread
    private fun startNewGame(config: GameConfig, localClientName: String?, onConnected: Runnable?) {
        if (config.server == null) {
            val ret = runServerForNewGame(
                config.gameMode,
                config.fieldSize,
                config.stones,
                config.difficulty
            )
            if (ret != 0) {
                DependencyProvider.crashReporter().log("Error starting server: $ret")
            }
        }
        viewModel.disconnectClient()
        val board = Board()
        val game = Game(board)
        val requestGameStart = !config.showLobby
        board.startNewGame(config.gameMode, config.fieldSize, config.fieldSize)
        setGameClient(GameClient(game, config))
        viewModel.startConnectingClient(config, localClientName, Runnable {
            if (onConnected != null) onConnected.run()
            if (requestGameStart) client?.requestGameStart()
        })
    }

    @Throws(Exception::class)
    private fun restoreOldGame() {
        try {
            val fis = openFileInput(GAME_STATE_FILE)
            val bos = ByteArrayOutputStream()
            val p = Parcel.obtain()
            val b = ByteArray(1024)
            var bytesRead: Int
            while ((fis.read(b).also { bytesRead = it }) != -1) {
                bos.write(b, 0, bytesRead)
            }
            fis.close()
            val bytes = bos.toByteArray()
            bos.close()
            p.unmarshall(bytes, 0, bytes.size)
            p.setDataPosition(0)
            val bundle = p.readBundle(FreebloksActivity::class.java.classLoader)
            p.recycle()
            deleteFile(GAME_STATE_FILE)
            if (bundle != null) {
                readStateFromBundle(bundle)
            }
        } catch (fe: FileNotFoundException) {
            /* signal non-failure if game state file is missing */
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun saveGameState(filename: String) {
        val p = Parcel.obtain()
        val b = Bundle()
        val client = client ?: return
        synchronized(client) {
            writeStateToBundle(b)
            p.writeBundle(b)
        }
        object : Thread() {
            override fun run() {
                try {
                    openFileOutput(filename, Context.MODE_PRIVATE).use {
                        it.write(p.marshall())
                    }
                    p.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.game_optionsmenu, menu)
        optionsMenu = menu
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.undo).isEnabled = (viewModel.canRequestUndo.value) ?: false
        menu.findItem(R.id.hint).isEnabled = (viewModel.canRequestHint.value) ?: false
        soundEnabledChanged(viewModel.soundsEnabled)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun soundEnabledChanged(enabled: Boolean) {
        optionsMenu?.run {
            findItem(R.id.sound_toggle_button).setTitle(if (enabled) R.string.sound_on else R.string.sound_off)
            findItem(R.id.sound_toggle_button).setIcon(if (enabled) R.drawable.ic_volume_up_white_48dp else R.drawable.ic_volume_off_white_48dp)
        }
    }

    override fun showMainMenu() {
        MainMenu().show(supportFragmentManager, "game_menu")
    }

    private fun dismissMainMenu() {
        val f = supportFragmentManager.findFragmentByTag("game_menu") as DialogFragment?
        f?.dismiss()
    }

    override fun onCreateDialog(id: Int, args: Bundle): Dialog? {
        val builder: MaterialAlertDialogBuilder
        when (id) {
            DIALOG_QUIT -> {
                builder = MaterialAlertDialogBuilder(this)
                builder.setMessage(R.string.do_you_want_to_leave_current_game)
                builder.setPositiveButton(android.R.string.yes) { _, _ -> showMainMenu() }
                builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                return builder.create()
            }
            DIALOG_NEW_GAME_CONFIRMATION -> {
                builder = MaterialAlertDialogBuilder(this)
                builder.setMessage(R.string.do_you_want_to_leave_current_game)
                builder.setPositiveButton(android.R.string.yes, { dialog: DialogInterface?, which: Int -> startNewDefaultGame() })
                builder.setNegativeButton(android.R.string.no, { dialog: DialogInterface, which: Int -> dialog.dismiss() })
                return builder.create()
            }
            else -> return super.onCreateDialog(id)
        }
    }

    override fun onStartClientGameWithConfig(config: GameConfig, localClientName: String?, runAfter: Runnable?) {
        dismissMainMenu()
        startNewGame(config, localClientName, runAfter)
    }

    override fun onConnectToBluetoothDevice(config: GameConfig, localClientName: String?, device: BluetoothDevice) {
        dismissMainMenu()
        viewModel.disconnectClient()
        val board = Board()
        val game = Game()
        board.startNewGame(config.gameMode, config.fieldSize, config.fieldSize)
        setGameClient(GameClient(game, config))
        viewModel.startConnectingBluetooth(device, localClientName)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val client = client
        val intent: Intent
        when (item.itemId) {
            android.R.id.home -> {
                showMainMenu()
                viewModel.intro?.cancel()
                return true
            }
            R.id.new_game -> {
                if (viewModel.intro != null) viewModel.intro?.cancel() else {
                    if (client == null || client.game.isFinished) startNewDefaultGame() else showDialog(DIALOG_NEW_GAME_CONFIRMATION)
                }
                return true
            }
            R.id.preferences -> {
                intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.sound_toggle_button -> {
                viewModel.toggleSound()
                return true
            }
            R.id.hint -> {
                if (client == null) return true
                findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
                findViewById<View>(R.id.movesLeft).visibility = View.INVISIBLE
                scene.currentStone.stopDragging()
                client.requestHint()
                return true
            }
            R.id.undo -> {
                if (client == null) return true
                scene.clearEffects()
                client.requestUndo()
                scene.soundPool.play(scene.soundPool.SOUND_UNDO, 1.0f, 1.0f)
                return true
            }
            R.id.show_main_menu -> {
                val lastStatus = lastStatus
                if ((client != null) && client.game.isStarted && (lastStatus != null) && (lastStatus.clients > 1)) showDialog(DIALOG_QUIT) else {
                    showMainMenu()
                    viewModel.intro?.cancel()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * @param data the data to show
     */
    @UiThread
    private fun playerSheetChanged(data: SheetPlayer) {
        val client = client
        val myLocation = findViewById<View>(R.id.myLocation)
        if (data.isRotated && (client != null) && !client.game.isFinished) {
            myLocation.visibility = View.VISIBLE
        } else {
            myLocation.visibility = View.INVISIBLE
        }
    }

    override fun newCurrentPlayer(player: Int) {}

    /* we have to store the number of possible turns before and after a stone has been set
	 * to detect blocking of other players */
    @Deprecated("")
    private val numberOfPossibleTurns = IntArray(4)

    override fun stoneWillBeSet(turn: Turn) {
        val client = client ?: return
        for (i in 0..3) numberOfPossibleTurns[i] = client.game.board.getPlayer(i).numberOfPossibleTurns
    }

    override fun stoneHasBeenSet(turn: Turn) {
        val game = client?.game ?: return
        val board = game.board
        runOnUiThread(Runnable {
            if (!game.isLocalPlayer(turn.player)) {
                scene.soundPool.play(scene.soundPool.SOUND_CLICK1, 1.0f, 0.9f + Math.random().toFloat() * 0.2f)
                viewModel.vibrate(Global.VIBRATE_SET_STONE.toLong())
            }
        })
        for (i in 0..3) {
            val p = board.getPlayer(i)
            if (p.numberOfPossibleTurns <= 0 && numberOfPossibleTurns[i] > 0) {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        if (view != null) {
                            val playerName = viewModel.getPlayerName(p.number)
                            Toast.makeText(this@FreebloksActivity, getString(R.string.color_is_out_of_moves, playerName), Toast.LENGTH_SHORT).show()
                            scene.soundPool.play(scene.soundPool.SOUND_PLAYER_OUT, 0.8f, 1.0f)
                            if (scene.hasAnimations()) {
                                val sx: Int
                                val sy: Int
                                val gameMode = game.gameMode
                                sx = board.getPlayerSeedX(p.number, gameMode)
                                sy = board.getPlayerSeedY(p.number, gameMode)
                                for (x in 0 until board.width) for (y in 0 until board.height) if (board.getFieldPlayer(y, x) == p.number) {
                                    var effected = false
                                    synchronized(scene.effects) {
                                        for (j in scene.effects.indices) if (scene.effects.get(j).isEffected(x, y)) {
                                            effected = true
                                            break
                                        }
                                    }
                                    if (!effected) {
                                        val distance = Math.sqrt((x - sx) * (x - sx) + (y - sy) * (y - sy).toDouble()).toFloat()
                                        val effect: Effect = BoardStoneGlowEffect(
                                            (scene),
                                            scene.getPlayerColor(p.number),
                                            x,
                                            y,
                                            distance)
                                        scene.addEffect(effect)
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    override fun hintReceived(turn: Turn) {
        analytics.logEvent("hint_received", null)
        runOnUiThread(object : Runnable {
            override fun run() {
                findViewById<View>(R.id.progressBar).visibility = View.INVISIBLE
                findViewById<View>(R.id.movesLeft).visibility = View.VISIBLE
            }
        })
    }

    override fun gameFinished() {
        val client = client ?: return
        val lastStatus = lastStatus ?: return

        deleteFile(GAME_STATE_FILE)
        val b = Bundle()
        b.putString("server", client.config.server)
        b.putString("game_mode", client.game.gameMode.toString())
        b.putInt("w", client.game.board.width)
        b.putInt("h", client.game.board.height)
        b.putInt("clients", lastStatus.clients)
        b.putInt("players", lastStatus.player)
        analytics.logEvent("game_finished", b)

        /* TODO: play sound on game finish? */
        runOnUiThread(object : Runnable {
            override fun run() {
                val args = Bundle()
                args.putSerializable("game", client.game)
                args.putSerializable("lastStatus", lastStatus)
                val d: DialogFragment = GameFinishFragment()
                d.arguments = args

                // this is not ideal but avoids a crash when the game finishes while the activity
                // is in the background. Maybe look into having observable events that adhere to lifecycle.
                supportFragmentManager
                    .beginTransaction()
                    .add(d, null)
                    .commitAllowingStateLoss()
            }
        })
    }

    override fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (hasWindowFocus()) {
                    /* only animate chatButton, if no dialog has focus */
                    /* TODO: animate if activity is stopped or paused? */
                    val a: Animation = AlphaAnimation(0.4f, 1.0f)
                    a.duration = 350
                    a.repeatCount = Animation.INFINITE
                    a.repeatMode = Animation.REVERSE
                    chatButton.startAnimation(a)
                }
            }
        })
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {}

    override fun playerLeft(client: Int, player: Int, name: String?) {}

    override fun gameStarted() {
        val client = client ?: return
        val lastStatus = lastStatus ?: return

        gameStartTime = System.currentTimeMillis()
        val b = Bundle()
        b.putString("server", client.config.server)
        b.putString("game_mode", client.game.gameMode.toString())
        b.putInt("w", client.game.board.width)
        b.putInt("h", client.game.board.height)
        b.putInt("clients", lastStatus.clients)
        b.putInt("players", lastStatus.player)
        analytics.logEvent("game_started", b)
        if (lastStatus.clients >= 2) {
            analytics.logEvent("game_start_multiplayer", b)
        }
        Log.d(tag, "Game started")
        for (i in 0 until Board.PLAYER_MAX) if (client.game.isLocalPlayer(i)) Log.d(tag, "Local player: $i")
    }

    override fun stoneUndone(t: Turn) {
        analytics.logEvent("undo_move", null)
    }

    override fun serverStatus(status: MessageServerStatus) {
        lastStatus = status
        if (status.clients > 1) {
            chatButton.post { chatButton.visibility = View.VISIBLE }
        }
    }

    @WorkerThread
    override fun onConnected(client: GameClient) {
        if (client.config.showLobby) {
            runOnUiThread {
                val bundle = Bundle()
                val server: String = client.config.server ?: "localhost"
                bundle.putString("server", server)
                analytics.logEvent("show_lobby", bundle)
                LobbyDialog().show(supportFragmentManager, null)
            }
        }
    }

    @WorkerThread
    override fun onConnectionFailed(client: GameClient, error: Exception) {
        runOnUiThread {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setMessage(error.message)
            builder.setOnDismissListener(DialogInterface.OnDismissListener { dialog: DialogInterface? -> showMainMenu() })
            builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog: DialogInterface, which: Int -> dialog.dismiss() })
            builder.show()
        }
    }

    @WorkerThread
    override fun onDisconnected(client: GameClient, error: Exception?) {
        Log.w(tag, "onDisconnected()")
        runOnUiThread(object : Runnable {
            override fun run() {
                lastStatus = null
                view.setGameClient(null)
                chatButton.visibility = View.INVISIBLE
                if (error != null) {
                    /* TODO: add sound on disconnect on error */
                    saveGameState(GAME_STATE_FILE)
                    val builder = MaterialAlertDialogBuilder(this@FreebloksActivity)
                    builder.setTitle(android.R.string.dialog_alert_title)
                    builder.setMessage(getString(R.string.disconnect_error, error.message))
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                    builder.setPositiveButton(android.R.string.ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            dialog.dismiss()
                            try {
                                restoreOldGame()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    builder.create().show()
                }
            }
        })
    }

    override fun onBackPressed() {
        val client = client
        val lastStatus = lastStatus

        if (undoWithBack && (client != null) && client.isConnected()) {
            scene.clearEffects()
            client.requestUndo()
            scene.soundPool.play(scene.soundPool.SOUND_UNDO, 1.0f, 1.0f)
            return
        }
        if ((client != null) && client.game.isStarted && !client.game.isFinished && (lastStatus != null) && (lastStatus.clients > 1)) showDialog(DIALOG_QUIT) else {
            if (viewModel.intro != null) {
                viewModel.intro?.cancel()
            } else {
                showMainMenu()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val client = client
        if ((Intent.ACTION_DELETE == intent.action)) {
            Log.d(tag, "ACTION_DELETE")
            finish()
        } else {
            if (intent.hasExtra("showChat") && (client != null) && client.game.isStarted) {
                LobbyDialog().show(supportFragmentManager, null)
            }
        }
    }

    override fun onLobbyDialogClosed() {
        val client = client
        if ((client != null) && !client.game.isStarted && !client.game.isFinished) {
            analytics.logEvent("lobby_close", null)
            viewModel.disconnectClient()
            showMainMenu()
        }
    }

    companion object {
        val tag = FreebloksActivity::class.java.simpleName

        private const val DIALOG_QUIT = 3
        private const val DIALOG_NEW_GAME_CONFIRMATION = 8
        private const val GAME_STATE_FILE = "gamestate.bin"
    }
}