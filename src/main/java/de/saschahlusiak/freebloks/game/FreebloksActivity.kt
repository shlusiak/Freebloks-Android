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
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import de.saschahlusiak.freebloks.game.lobby.LobbyDialogDelegate
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.model.GameConfig.Companion.defaultStonesForMode
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.ThemeManager.Companion.get
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import de.saschahlusiak.freebloks.view.scene.intro.IntroDelegate
import kotlinx.android.synthetic.main.main_3d.*
import kotlinx.android.synthetic.main.player_detail_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.lang.Runnable

class FreebloksActivity: AppCompatActivity(), GameEventObserver, IntroDelegate, OnStartCustomGameListener, LobbyDialogDelegate {
    private lateinit var view: Freebloks3DView
    private var client: GameClient? = null
    private var optionsMenu: Menu? = null
    private var showRateDialog = false
    private val analytics by lazy { DependencyProvider.analytics() }
    private lateinit var scene: Scene
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    // TODO: make private
    val viewModel: FreebloksActivityViewModel by lazy { ViewModelProvider(this).get(FreebloksActivityViewModel::class.java) }

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

        // TODO: move to App
        // must initialise before anything else
        DependencyProvider.initialise(this)

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

        this.client = viewModel.client
        val client = client
        viewModel.intro?.listener = this

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
            if (viewModel.showIntro) {
                viewModel.intro = Intro(applicationContext, scene, this)
            } else
                onIntroCompleted()
        }

        chatButton.setOnClickListener {
            chatButton.clearAnimation()
            LobbyDialog().show(supportFragmentManager, null)
        }

        myLocation.setOnClickListener { scene.boardObject.resetRotation() }

        viewModel.connectionStatus.observe(this) { onConnectionStatusChanged(it) }
        viewModel.playerToShowInSheet.observe(this) { onPlayerSheetChanged(it) }
        viewModel.soundsEnabledLiveData.observe(this) { onSoundEnabledChanged(it) }
        viewModel.canRequestHint.observe(this) { invalidateOptionsMenu() }
        viewModel.canRequestUndo.observe(this) { invalidateOptionsMenu() }
        viewModel.chatButtonVisible.observe(this) { chatButton.isVisible = it }
        viewModel.googleAccountSignedIn.observe(this) {
            viewModel.gameHelper.setWindowForPopups(window)
            if (Global.IS_VIP) {
                viewModel.gameHelper.unlock(getString(R.string.achievement_vip))
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(tag, "onRestoreInstanceState (bundle=$savedInstanceState)")
        super.onRestoreInstanceState(savedInstanceState)

        if (client == null) {
            readStateFromBundle(savedInstanceState)
        }
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        client?.removeObserver(this)
        client?.removeObserver((view))
        super.onDestroy()
    }

    override fun onPause() {
        Log.d(tag, "onPause")
        client?.run {
            if (game.isStarted && !game.isFinished) saveGameState(this)
        }
        super.onPause()
    }

    override fun onResume() {
        Log.d(tag, "onResume")
        super.onResume()
    }

    override fun onStop() {
        viewModel.onStop()
        view.onPause()
        prefs.edit()
            .putFloat("view_scale", view.getScale())
            .apply()

        Log.d(tag, "onStop")
        super.onStop()
    }

    override fun onStart() {
        Log.d(tag, "onStart")
        super.onStart()
        view.onResume()
        viewModel.reloadPreferences()

        scene.showSeeds = viewModel.showSeeds
        scene.showOpponents = viewModel.showOpponents
        scene.showAnimations = viewModel.showAnimations
        scene.snapAid = viewModel.snapAid

        val t = get(this).getTheme(prefs.getString("theme", "texture_wood"), ColorThemes.Blue) ?: ColorThemes.Blue
        view.setTheme(t)

        viewModel.onStart()

        /* update wheel in case showOpponents has changed */
        scene.wheel.update(scene.boardObject.showWheelPlayer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(tag, "onSaveInstanceState")
        super.onSaveInstanceState(outState)

        outState.putFloat("view_scale", view.getScale())
        outState.putBoolean("showRateDialog", showRateDialog)

        client?.run {
            writeStateToBundle(this, outState)
        }
    }

    private fun writeStateToBundle(client: GameClient, outState: Bundle) {
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
            onConnected?.run()
            if (requestGameStart) client?.requestGameStart()
        })
    }

    @Throws(Exception::class)
    private fun restoreOldGame() {
        try {
            val bytes = ByteArrayOutputStream().use { output ->
                openFileInput(GAME_STATE_FILE).use { input ->
                    input.copyTo(output)
                }
                output.toByteArray()
            }

            val p = Parcel.obtain()
            p.unmarshall(bytes, 0, bytes.size)
            p.setDataPosition(0)

            val bundle = p.readBundle(classLoader)
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

    private fun saveGameState(client: GameClient, filename: String = GAME_STATE_FILE) {
        val p = Parcel.obtain()
        val b = Bundle()
        synchronized(client) {
            writeStateToBundle(client, b)
            p.writeBundle(b)
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(p.marshall())
                }
                p.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateDialog(id: Int, args: Bundle): Dialog? {
        when (id) {
            DIALOG_QUIT -> {
                return MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.do_you_want_to_leave_current_game)
                    .setPositiveButton(android.R.string.yes) { _, _ -> showMainMenu() }
                    .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                    .create()
            }
            DIALOG_NEW_GAME_CONFIRMATION -> {
                return MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.do_you_want_to_leave_current_game)
                    .setPositiveButton(android.R.string.yes) { _, _ -> startNewDefaultGame() }
                    .setNegativeButton(android.R.string.no) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .create()
            }
            else -> return super.onCreateDialog(id)
        }
    }

    override fun onBackPressed() {
        val client = client
        val lastStatus = viewModel.lastStatus

        if (viewModel.undoWithBack && (client != null) && client.isConnected()) {
            scene.clearEffects()
            client.requestUndo()
            scene.soundPool.play(scene.soundPool.SOUND_UNDO, 1.0f, 1.0f)
            return
        }
        if ((client != null) && client.game.isStarted && !client.game.isFinished && (lastStatus != null) && (lastStatus.clients > 1))
            showDialog(DIALOG_QUIT)
        else {
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

    private fun dismissMainMenu() {
        val f = supportFragmentManager.findFragmentByTag("game_menu") as DialogFragment?
        f?.dismiss()
    }

    @UiThread
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

    @UiThread
    override fun onLobbyDialogCancelled() {
        val client = client
        if ((client != null) && !client.game.isStarted && !client.game.isFinished) {
            analytics.logEvent("lobby_close", null)
            viewModel.disconnectClient()
            showMainMenu()
        }
    }

    //region LiveData Observers

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
            }

            ConnectionStatus.Connected,
            ConnectionStatus.Failed,
            ConnectionStatus.Disconnected -> {
                f?.dismiss()
            }
        }
    }

    /**
     * The player to show in the bottom sheet has changed
     * @param data the new data to show
     */
    @UiThread
    private fun onPlayerSheetChanged(data: SheetPlayer) {
        val client = client
        if (data.isRotated && (client != null) && !client.game.isFinished) {
            myLocation.visibility = View.VISIBLE
        } else {
            myLocation.visibility = View.INVISIBLE
        }
    }

    private fun onSoundEnabledChanged(enabled: Boolean) {
        optionsMenu?.run {
            findItem(R.id.sound_toggle_button).setTitle(if (enabled) R.string.sound_on else R.string.sound_off)
            findItem(R.id.sound_toggle_button).setIcon(if (enabled) R.drawable.ic_volume_up_white_48dp else R.drawable.ic_volume_off_white_48dp)
        }
    }

    //endregion

    //region OnStartCustomGameListener

    override fun showMainMenu() {
        MainMenu().show(supportFragmentManager, "game_menu")
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

    //endregion

    //region Menu handling

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.game_optionsmenu, menu)
        optionsMenu = menu
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.undo).isEnabled = (viewModel.canRequestUndo.value) ?: false
        menu.findItem(R.id.hint).isEnabled = (viewModel.canRequestHint.value) ?: false
        onSoundEnabledChanged(viewModel.soundsEnabled)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val client = client
        val intent: Intent

        when (item.itemId) {
            android.R.id.home -> {
                showMainMenu()
                viewModel.intro?.cancel()
            }
            R.id.new_game -> {
                if (viewModel.intro != null) viewModel.intro?.cancel() else {
                    if (client == null || client.game.isFinished) startNewDefaultGame() else showDialog(DIALOG_NEW_GAME_CONFIRMATION)
                }
            }
            R.id.preferences -> {
                intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.sound_toggle_button -> {
                viewModel.toggleSound()
            }
            R.id.hint -> {
                if (client == null) return true
                progressBar.visibility = View.VISIBLE
                movesLeft.visibility = View.INVISIBLE
                scene.currentStone.stopDragging()
                client.requestHint()
            }
            R.id.undo -> {
                if (client == null) return true
                scene.clearEffects()
                client.requestUndo()
                scene.soundPool.play(scene.soundPool.SOUND_UNDO, 1.0f, 1.0f)
            }
            R.id.show_main_menu -> {
                val clients = viewModel.lastStatus?.clients ?: 0
                val isStarted = client?.game?.isStarted ?: false
                if (isStarted && clients > 1) showDialog(DIALOG_QUIT) else {
                    showMainMenu()
                    viewModel.intro?.cancel()
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //endregion

    //region GameEventObserver callbacks
    @WorkerThread
    override fun playerIsOutOfMoves(player: Player) {
        scene.soundPool.play(scene.soundPool.SOUND_PLAYER_OUT, 0.8f, 1.0f)

        runOnUiThread {
            val playerName = viewModel.getPlayerName(player.number)
            Toast.makeText(this@FreebloksActivity, getString(R.string.color_is_out_of_moves, playerName), Toast.LENGTH_SHORT).show()
        }
    }

    @WorkerThread
    override fun hintReceived(turn: Turn) {
        analytics.logEvent("hint_received", null)

        lifecycleScope.launch {
            progressBar.visibility = View.INVISIBLE
            movesLeft.visibility = View.VISIBLE
        }
    }

    @WorkerThread
    override fun gameFinished() {
        val client = client ?: return
        val lastStatus = viewModel.lastStatus ?: return

        GlobalScope.launch(Dispatchers.IO) {
            deleteFile(GAME_STATE_FILE)
        }

        val b = Bundle().apply {
            putString("server", client.config.server)
            putString("game_mode", client.game.gameMode.toString())
            putInt("w", client.game.board.width)
            putInt("h", client.game.board.height)
            putInt("clients", lastStatus.clients)
            putInt("players", lastStatus.player)
        }
        analytics.logEvent("game_finished", b)

        /* TODO: play sound on game finish? */
        lifecycleScope.launchWhenStarted {
            val dialog = GameFinishFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("game", client.game)
                    putSerializable("lastStatus", lastStatus)
                }
            }

            supportFragmentManager
                .beginTransaction()
                .add(dialog, null)
                .commitAllowingStateLoss()
        }
    }

    @WorkerThread
    override fun chatReceived(status: MessageServerStatus, client: Int, player: Int, message: String) {
        lifecycleScope.launch {
            if (hasWindowFocus()) {
                /* only animate chatButton, if no dialog has focus */
                val animation = AlphaAnimation(0.4f, 1.0f).apply {
                    duration = 350
                    repeatCount = Animation.INFINITE
                    repeatMode = Animation.REVERSE
                }
                chatButton.startAnimation(animation)
            }
        }
    }

    @WorkerThread
    override fun gameStarted() {
        val client = client ?: return
        val lastStatus = viewModel.lastStatus ?: return

        val b = Bundle().apply {
            putString("server", client.config.server)
            putString("game_mode", client.game.gameMode.toString())
            putInt("w", client.game.board.width)
            putInt("h", client.game.board.height)
            putInt("clients", lastStatus.clients)
            putInt("players", lastStatus.player)
        }

        analytics.logEvent("game_started", b)
        if (lastStatus.clients >= 2) {
            analytics.logEvent("game_start_multiplayer", b)
        }

        Log.d(tag, "Game started")

        for (i in 0 until Board.PLAYER_MAX)
            if (client.game.isLocalPlayer(i))
                Log.d(tag, "Local player: $i")
    }

    @WorkerThread
    override fun stoneUndone(t: Turn) {
        analytics.logEvent("undo_move", null)
    }

    @WorkerThread
    override fun onConnected(client: GameClient) {
        if (client.config.showLobby) {
            val server = client.config.server ?: "localhost"
            val bundle = Bundle().apply {
                putString("server", server)
            }
            analytics.logEvent("show_lobby", bundle)

            lifecycleScope.launchWhenStarted {
                LobbyDialog().show(supportFragmentManager, null)
            }
        }
    }

    @WorkerThread
    override fun onConnectionFailed(client: GameClient, error: Exception) {
        lifecycleScope.launchWhenStarted {
            MaterialAlertDialogBuilder(this@FreebloksActivity)
                .setMessage(error.message)
                .setOnDismissListener { showMainMenu() }
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
        }
    }

    @WorkerThread
    override fun onDisconnected(client: GameClient, error: Exception?) {
        Log.w(tag, "onDisconnected()")
        lifecycleScope.launch {
            view.setGameClient(null)

            if (error != null) {
                /* TODO: add sound on disconnect on error */
                saveGameState(client)

                MaterialAlertDialogBuilder(this@FreebloksActivity)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(getString(R.string.disconnect_error, error.message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                        try {
                            restoreOldGame()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .create().show()
            }
        }
    }
    //endregion

    companion object {
        val tag = FreebloksActivity::class.java.simpleName

        private const val DIALOG_QUIT = 3
        private const val DIALOG_NEW_GAME_CONFIRMATION = 8
        private const val GAME_STATE_FILE = "gamestate.bin"
    }
}