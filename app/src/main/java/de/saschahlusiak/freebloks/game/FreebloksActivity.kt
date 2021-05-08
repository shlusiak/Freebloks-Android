package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.transition.TransitionManager
import android.util.Log
import android.view.*
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
import androidx.lifecycle.viewModelScope
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
import de.saschahlusiak.freebloks.game.dialogs.RateAppFragment
import de.saschahlusiak.freebloks.game.dialogs.RateAppFragment.Companion.shouldShowRateDialog
import de.saschahlusiak.freebloks.game.finish.GameFinishFragment
import de.saschahlusiak.freebloks.game.lobby.LobbyDialog
import de.saschahlusiak.freebloks.game.lobby.LobbyDialogDelegate
import de.saschahlusiak.freebloks.model.Board
import de.saschahlusiak.freebloks.model.Game
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameConfig.Companion.defaultStonesForMode
import de.saschahlusiak.freebloks.model.GameStateException
import de.saschahlusiak.freebloks.model.Player
import de.saschahlusiak.freebloks.network.ProtocolException
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.preferences.SettingsActivity
import de.saschahlusiak.freebloks.theme.ColorThemes
import de.saschahlusiak.freebloks.theme.FeedbackType
import de.saschahlusiak.freebloks.theme.ThemeManager
import de.saschahlusiak.freebloks.view.Freebloks3DView
import de.saschahlusiak.freebloks.view.scene.Scene
import de.saschahlusiak.freebloks.view.scene.intro.Intro
import de.saschahlusiak.freebloks.view.scene.intro.IntroDelegate
import kotlinx.android.synthetic.main.freebloks_activity.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class FreebloksActivity: AppCompatActivity(), GameEventObserver, IntroDelegate, OnStartCustomGameListener, LobbyDialogDelegate {
    private lateinit var view: Freebloks3DView
    private var showRateDialog = false
    private val analytics by lazy { DependencyProvider.analytics() }
    private lateinit var scene: Scene
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    private var menuShown = false

    private val viewModel: FreebloksActivityViewModel by lazy { ViewModelProvider(this).get(FreebloksActivityViewModel::class.java) }

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

        super.onCreate(savedInstanceState)

        setContentView(R.layout.freebloks_activity)

        view = findViewById(R.id.board)
        scene = Scene(viewModel, viewModel.intro, viewModel.sounds)
        view.setScene(scene)

        volumeControlStream = AudioManager.STREAM_MUSIC

        viewModel.intro?.listener = this

        if (savedInstanceState != null) {
            view.setScale(savedInstanceState.getFloat("view_scale", 1.0f))
            showRateDialog = savedInstanceState.getBoolean("showRateDialog", false)
        } else {
            view.setScale(prefs.getFloat("view_scale", 1.0f))
            showRateDialog = shouldShowRateDialog(this)
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

        val client = viewModel.client
        if (client != null) {
            // game in progress during configuration change
            client.addObserver(this)
            client.addObserver(view)
            view.setGameClient(client)
        } else if (savedInstanceState == null) {
            if (viewModel.showIntro) {
                viewModel.intro = Intro(applicationContext, scene, this)
                scene.intro = viewModel.intro
            } else {
                onIntroCompleted()
            }
        }

        menuOverlayContainer.isVisible = (viewModel.intro == null)

        menuOverlayContainer.setOnApplyWindowInsetsListener { v: View, insets: WindowInsets ->
            v.setPadding(0, findTopPaddingForView(insets, Gravity.RIGHT), insets.systemWindowInsetRight, 0)
            insets
        }

        chatButton.setOnClickListener {
            analytics.logEvent("game_chat_click", null)

            chatButton.clearAnimation()
            LobbyDialog().show(supportFragmentManager, null)
        }

        myLocation.setOnClickListener { onResetRotationButtonClick() }
        menuButton.setOnClickListener { onMenuButtonClick() }
        soundOnOff.setOnClickListener { onSoundButtonClick() }
        hintButton.setOnClickListener { onHintButtonClick() }
        undoButton.setOnClickListener { onUndoButtonClick() }
        newGameButton.setOnClickListener { onNewGameButtonClick() }
        preferencesButton.setOnClickListener { onPreferencesButtonClick() }
        view.setOnTouchListener { _, _ ->
            if (menuShown) {
                showMenu(shown = false, animate = true)
            }
            false
        }

        viewModel.connectionStatus.observe(this) { onConnectionStatusChanged(it) }
        viewModel.playerToShowInSheet.observe(this) { onPlayerSheetChanged(it) }
        viewModel.soundsEnabledLiveData.observe(this) { onSoundEnabledChanged(it) }
        viewModel.canRequestHint.observe(this) { hintButton.isEnabled = it }
        viewModel.canRequestUndo.observe(this) { undoButton.isEnabled = it }
        viewModel.chatButtonVisible.observe(this) { chatButtonContainer.isVisible = it }
        viewModel.googleAccountSignedIn.observe(this) {
            viewModel.gameHelper.setWindowForPopups(window)
            if (Global.IS_VIP) {
                viewModel.gameHelper.unlock(getString(R.string.achievement_vip))
            }
        }

        showMenu(shown = false, animate = false)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(tag, "onRestoreInstanceState (bundle=$savedInstanceState)")
        super.onRestoreInstanceState(savedInstanceState)

        if (viewModel.client == null) {
            val game = savedInstanceState.getSerializable("game") as? Game
            if (game != null) {
                resumeGame(game)
            }
        }
    }

    override fun onDestroy() {
        Log.d(tag, "onDestroy")
        viewModel.client?.run {
            removeObserver(this@FreebloksActivity)
            removeObserver(view)
        }
        super.onDestroy()
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

        val tm = ThemeManager.get(this)
        val background = tm.getTheme(prefs.getString("theme", "texture_wood"), ColorThemes.Blue)
        val board = tm.getTheme(prefs.getString("board_theme", "field_wood"), ColorThemes.White)
        view.setTheme(background, board)

        viewModel.onStart()

        /* update wheel in case showOpponents has changed */
        scene.wheel.update(scene.boardObject.showWheelPlayer)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(tag, "onSaveInstanceState")
        super.onSaveInstanceState(outState)

        outState.putFloat("view_scale", view.getScale())
        outState.putBoolean("showRateDialog", showRateDialog)

        val client = viewModel.client
        client?.run {
            val game = client.game
            synchronized(client) {
                if (game.isStarted && !game.isFinished) {
                    outState.putSerializable("game", game)
                }
            }
        }
    }

    private fun findTopPaddingForView(insets: WindowInsets, gravity: Int): Int {
        val padding = insets.systemWindowInsetTop

        // unfortunately we use full screen everywhere, which would make the top padding 0
        // so on devices with a display cutout with API level < 28, we unfortunately overlap the cutout
        // FIXME: behaviour on Android 8 on a device with cutout?
        if (Build.VERSION.SDK_INT < 28) {
            return padding
        }
        val cutout = insets.displayCutout ?: return padding
        val rects = cutout.boundingRects
        val displaySize = resources.displayMetrics.widthPixels
        for (rect in rects) {
            if (rect.top == 0) {
                // only interested in top cutouts
                when (gravity) {
                    Gravity.LEFT -> if (rect.left == 0 && insets.systemWindowInsetLeft == 0) return rect.bottom
                    Gravity.CENTER_HORIZONTAL -> if (rect.left != 0 && rect.right != displaySize) return rect.bottom
                    Gravity.RIGHT -> if (rect.right == displaySize && insets.systemWindowInsetRight == 0) return rect.bottom
                }
            }
        }
        return 0
    }

    @Suppress("SameParameterValue")
    private fun showFloatingMenuLabel(anchor: View, gravity: Int, show: Boolean, label: String) {
        var v = anchor.tag as? FloatingMenuLabel
        if (v == null) {
            v = FloatingMenuLabel(this, menuOverlayContainer, anchor, gravity)
            anchor.tag = v
        }
        v.setText(label)
        if (show) {
            v.show()
        } else {
            v.hide()
        }
    }

    private fun showMenu(shown: Boolean, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(menuOverlayContainer)
        }
        val visible = if (shown) View.VISIBLE else View.GONE

        preferencesButton.visibility = visible
        newGameButton.visibility = visible

        showFloatingMenuLabel(preferencesButton, Gravity.LEFT, shown, getString(R.string.settings))
        showFloatingMenuLabel(newGameButton, Gravity.LEFT, shown, getString(R.string.new_game))

        menuShown = shown
    }

    private fun onMenuButtonClick() {
        showMenu(!menuShown, true)
    }

    /**
     * Either starts a game with exactly the last config or a new default game.
     *
     * Called e.g. during long-press, "start new game" in the finish dialog, or on initial startup.
     */
    override fun startNewDefaultGame() {
        viewModel.viewModelScope.launch {
            val config = viewModel.client?.config
            if (config != null) {
                // when starting a new game from the options menu, keep previous config
                startNewGame(config, viewModel.localClientNameOverride)
            } else {
                // else start default game
                startNewGame(GameConfig(), null)
            }
        }
    }

    private fun setGameClient(client: GameClient) {
        client.addObserver(this)
        client.addObserver(view)
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
        viewModel.viewModelScope.launch {
            viewModel.connectToHost(config, null, false)
        }
    }

    @UiThread
    private fun startNewGame(config: GameConfig, localClientName: String?): Job {
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
        board.startNewGame(config.gameMode, config.stones, config.fieldSize, config.fieldSize)
        setGameClient(GameClient(game, config))

        return viewModel.viewModelScope.launch {
            viewModel.connectToHost(config, localClientName, requestGameStart)
        }
    }

    @Throws(Exception::class)
    private fun restoreOldGame() {
        try {
            val game = viewModel.loadGameState() ?: return
            resumeGame(game)
        } catch (fe: FileNotFoundException) {
            /* signal non-failure if game state file is missing */
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun onCreateDialog(id: Int, args: Bundle?): Dialog? {
        when (id) {
            DIALOG_QUIT -> {
                return MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.do_you_want_to_leave_current_game)
                    .setPositiveButton(android.R.string.yes) { _, _ -> showMainMenu() }
                    .setNegativeButton(android.R.string.no, null)
                    .create()
            }
            DIALOG_NEW_GAME_CONFIRMATION -> {
                return MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.do_you_want_to_leave_current_game)
                    .setPositiveButton(android.R.string.yes) { _, _ -> startNewDefaultGame() }
                    .setNegativeButton(android.R.string.no, null)
                    .create()
            }
            else -> return super.onCreateDialog(id, args)
        }
    }

    override fun onBackPressed() {
        val client = viewModel.client
        val lastStatus = viewModel.lastStatus

        if (menuShown) {
            showMenu(shown = false, animate = true)
            return
        } else if ((client != null) && client.game.isStarted && !client.game.isFinished && (lastStatus != null) && (lastStatus.clients > 1)) {
            showDialog(DIALOG_QUIT)
        } else {
            if (viewModel.intro != null) {
                viewModel.intro?.cancel()
            } else {
                showMainMenu()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?: return

        val client = viewModel.client
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
        Log.d(tag, "onIntroCompleted")
        menuOverlayContainer.isVisible = true

        viewModel.intro = null
        scene.intro = null
        viewModel.setSheetPlayer(-1, false)
        try {
            restoreOldGame()
        } catch (e: Exception) {
            Toast.makeText(this@FreebloksActivity, R.string.could_not_restore_game, Toast.LENGTH_LONG).show()
        }
        val client = viewModel.client
        val canResume = ((client != null) && client.game.isStarted && !client.game.isFinished)
        if (!canResume || !prefs.getBoolean("auto_resume", false)) showMainMenu()
        if (showRateDialog) RateAppFragment().show(supportFragmentManager, null)
        view.requestRender()
    }

    @UiThread
    override fun onLobbyDialogCancelled() {
        val client = viewModel.client
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
        val client = viewModel.client
        if (data.isRotated && (client?.game?.isFinished == false)) {
            myLocationContainer.visibility = View.VISIBLE
        } else {
            myLocationContainer.visibility = View.INVISIBLE
        }
    }

    private fun onSoundEnabledChanged(enabled: Boolean) {
        soundOnOff.setImageResource(if (enabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off)
    }

    //endregion

    //region OnStartCustomGameListener

    override fun showMainMenu() {
        MainMenuFragment().show(supportFragmentManager, "game_menu")
    }

    override fun onStartClientGameWithConfig(config: GameConfig, localClientName: String?): Job {
        dismissMainMenu()
        return startNewGame(config, localClientName)
    }

    override fun onConnectToBluetoothDevice(config: GameConfig, localClientName: String?, device: BluetoothDevice) {
        dismissMainMenu()
        viewModel.disconnectClient()

        val board = Board()
        val game = Game()
        board.startNewGame(config.gameMode, config.stones, config.fieldSize, config.fieldSize)
        setGameClient(GameClient(game, config))

        viewModel.connectToBluetooth(device, localClientName)
    }

    //endregion

    //region Menu handling

    private fun onResetRotationButtonClick() {
        analytics.logEvent("game_reset_rotation_click")

        scene.boardObject.resetRotation()
    }

    private fun onNewGameButtonClick() {
        analytics.logEvent("game_new_game_click")

        showMenu(shown = false, animate = true)
        if (viewModel.intro != null) viewModel.intro?.cancel() else {
            val client = viewModel.client
            if (client == null || client.game.isFinished) startNewDefaultGame() else showDialog(DIALOG_NEW_GAME_CONFIRMATION)
        }
    }

    private fun onHintButtonClick() {
        analytics.logEvent("game_hint_click")

        showMenu(shown = false, animate = true)
        scene.currentStone.stopDragging()
        viewModel.requestHint()
    }

    private fun onSoundButtonClick() {
        analytics.logEvent("game_sound_click")

        val soundOn = viewModel.toggleSound()
        Toast.makeText(this, if (soundOn) R.string.sound_on else R.string.sound_off, Toast.LENGTH_SHORT).show()
    }

    private fun onUndoButtonClick() {
        analytics.logEvent("game_undo_click")

        showMenu(shown = false, animate = true)
        viewModel.requestUndo()
        scene.playSound(FeedbackType.UndoStone)
    }

    private fun onPreferencesButtonClick() {
        analytics.logEvent("game_settings_click")

        showMenu(shown = false, animate = true)
        intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    //endregion

    //region GameEventObserver callbacks

    @WorkerThread
    override fun playerIsOutOfMoves(player: Player) {
        scene.playSound(FeedbackType.OutOfMoves, volume = 0.8f)

        lifecycleScope.launchWhenStarted {
            val playerName = viewModel.getPlayerName(player.number)
            Toast.makeText(this@FreebloksActivity, getString(R.string.color_is_out_of_moves, playerName), Toast.LENGTH_SHORT).show()
        }
    }

    @WorkerThread
    override fun gameFinished() {
        val client = viewModel.client ?: return
        val lastStatus = viewModel.lastStatus ?: return

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

    @UiThread
    override fun onConnected(client: GameClient) {
        if (client.config.showLobby) {
            val server = client.config.server ?: "localhost"
            val bundle = Bundle().apply {
                putString("server", server)
            }
            analytics.logEvent("lobby_show", bundle)

            lifecycleScope.launchWhenStarted {
                LobbyDialog().show(supportFragmentManager, null)
            }
        }
    }

    @UiThread
    override fun onConnectionFailed(client: GameClient, error: Exception) {
        lifecycleScope.launchWhenStarted {
            MaterialAlertDialogBuilder(this@FreebloksActivity)
                .setTitle(R.string.connection_refused)
                .setMessage("${error.javaClass.simpleName}: ${error.message}")
                .setOnDismissListener { showMainMenu() }
                .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .show()
        }
    }

    @UiThread
    override fun onDisconnected(client: GameClient, error: Throwable?) {
        Log.w(tag, "onDisconnected()")
        view.setGameClient(null)

        if (error != null) {
            when(error) {
                // these two are fatal and cause an app crash, so we get reports in Crashlytics
                is GameStateException, is ProtocolException -> throw RuntimeException(error)
            }

            /* TODO: add sound on disconnect on error */
            viewModel.saveGameState()

            lifecycleScope.launchWhenStarted {
                MaterialAlertDialogBuilder(this@FreebloksActivity)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(getString(R.string.disconnect_error, error.message))
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
    }
}