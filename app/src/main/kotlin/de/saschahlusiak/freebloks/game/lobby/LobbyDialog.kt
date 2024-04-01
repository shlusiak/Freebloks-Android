package de.saschahlusiak.freebloks.game.lobby

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.databinding.EditNameDialogBinding
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.colorOf
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.model.defaultStoneSet
import de.saschahlusiak.freebloks.network.message.MessageServerStatus

class LobbyDialog : DialogFragment(), GameEventObserver, OnItemClickListener, ColorAdapter.EditPlayerNameListener {

    private val viewModel: FreebloksActivityViewModel by viewModels(ownerProducer = { requireActivity() })
    private val client get() = viewModel.client
    private val listener get() = activity as LobbyDialogDelegate

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val client = client
            if (client != null && client.game.isStarted) {
                /* in-game chat */
                setCanceledOnTouchOutside(true)
            } else {
                /* lobby */
                setCanceledOnTouchOutside(false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setBackgroundDrawable(null)
        view as ComposeView

        view.setContent {
            Content()
        }

        val client = client
        if (client == null) {
            dismiss()
            return
        }

        client.addObserver(this@LobbyDialog)
    }

    @Composable
    private fun Content() {
        val client = client
        AppTheme {
            val lastStatus = viewModel.lastStatus.collectAsState()
            val chatHistory = viewModel.chatHistoryAsLiveData.asFlow().collectAsState(initial = emptyList())

            val players = remember(lastStatus) {
                derivedStateOf { getPlayerColors(lastStatus.value) }
            }

            LobbyScreen(
                isRunning = client != null && client.game.isStarted,
                status = lastStatus.value,
                chatHistory = chatHistory.value,
                players = players.value,
                onGameMode = { requestMode(gameMode = it) },
                onSize = { requestMode(size = it) },
                onTogglePlayer = ::onTogglePlayer,
                onChat = { sendChat(it) },
                onStart = { client?.requestGameStart() }
            )
        }
    }

    private fun onTogglePlayer(player: Int) {
        val client = client ?: return
        if (client.game.isLocalPlayer(player)) {
            client.revokePlayer(player)
        } else {
            client.requestPlayer(player, null)
        }
    }

    private fun getPlayerColors(status: MessageServerStatus?): List<PlayerColor> {
        status ?: return emptyList()
        val game = viewModel.game ?: return emptyList()

        val mode = status.gameMode
        val colors = mode.colors

        return (0 until colors).map { index ->
            val player = if (colors == 2) index * 2 else index
            PlayerColor(
                player = player,
                color = mode.colorOf(player),
                client = status.clientForPlayer[player],
                name = status.getPlayerName(player),
                isLocal = game.isLocalPlayer(player)
            )
        }
    }

    private fun sendChat(message: String) {
        // FIXME: The server cuts off the last character, so we have to append a new-line
        message.chunked(240).forEach {
            client?.sendChat(it + "\n")
        }
    }

    private fun requestMode(gameMode: GameMode? = null, size: Int? = null) {
        val status = viewModel.lastStatus.value ?: return
        val newMode = gameMode ?: status.gameMode
        val newSize = size ?: gameMode?.defaultBoardSize() ?: status.size

        if (newMode == status.gameMode && size == status.size) return

        client?.requestGameMode(
            width = newSize,
            height = newSize,
            gameMode = newMode,
            stones = newMode.defaultStoneSet()
        )
    }

    override fun onDestroyView() {
        client?.removeObserver(this)

        super.onDestroyView()
    }

    override fun onStart() {
        super.onStart()

        val client = client
        if (client == null) {
            /* this can happen when the app is saved but purged from memory
             * upon resume, the open dialog is reopened but the client connection
             * has been disconnected. just close the lobby since there is no
             * connection
             */

            dismiss()
            return
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        listener.onLobbyDialogCancelled()
    }

    override fun onEditPlayerName(player: Int) {
        val client = client ?: return
        val lastStatus = client.lastStatus ?: return

        val dialogBinding = EditNameDialogBinding.inflate(layoutInflater, null, false)
        val edit = dialogBinding.edit.apply {
            setText(lastStatus.getClientName(lastStatus.getClient(player)))
        }

        // FIXME
        MaterialAlertDialogBuilder(requireContext()).apply {
            setView(dialogBinding.root)
            setTitle(R.string.prefs_player_name)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val name = edit.text.toString().trim()
                PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString("player_name", name)
                    .apply()

                viewModel.reloadPreferences()

                client.revokePlayer(player)
                client.requestPlayer(player, name)
            }
            setNegativeButton(android.R.string.cancel) { _, _ -> }
        }.show().apply {
            edit.selectAll()
            edit.requestFocus()

            window?.clearFlags(FLAG_NOT_FOCUSABLE or FLAG_ALT_FOCUSABLE_IM)
            window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val client = client ?: return
        if (client.game.isStarted) return

        if (client.game.isLocalPlayer(id.toInt())) {
            client.revokePlayer(id.toInt())
        } else {
            client.requestPlayer(id.toInt(), null)
        }
    }

    override fun gameStarted() {
        lifecycleScope.launchWhenStarted {
            dismiss()
        }
    }

    override fun onDisconnected(client: GameClient, error: Throwable?) {
        lifecycleScope.launchWhenStarted {
            dismiss()
        }
    }
}