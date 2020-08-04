package de.saschahlusiak.freebloks.game.lobby

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.game.ConnectionStatus
import de.saschahlusiak.freebloks.game.FreebloksActivityViewModel
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.model.defaultStoneSet
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import kotlinx.android.synthetic.main.edit_name_dialog.view.*
import kotlinx.android.synthetic.main.lobby_dialog.*

class LobbyDialog: MaterialDialogFragment(R.layout.lobby_dialog), GameEventObserver, OnItemClickListener, ColorAdapter.EditPlayerNameListener {

    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(FreebloksActivityViewModel::class.java) }
    private val client get() = viewModel.client
    private val listener get() = activity as LobbyDialogDelegate

    private var chatAdapter: ChatListAdapter? = null
    private var colorAdapter: ColorAdapter? = null

    private val handler = Handler()

    private val gameModeSelectedListener = object : OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val client = client ?: return

            val gameMode = from(game_mode.selectedItemPosition)
            val size = gameMode.defaultBoardSize()
            val stones = gameMode.defaultStoneSet()

            client.requestGameMode(size, size, gameMode, stones)
        }
    }

    private val sizeSelectedListener = object : OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val client = client ?: return
            val gameMode = client.game.gameMode

            val size = GameConfig.FIELD_SIZES[field_size.selectedItemPosition]

            val stones = when (gameMode) {
                GameMode.GAMEMODE_JUNIOR -> GameConfig.JUNIOR_STONE_SET
                else -> GameConfig.DEFAULT_STONE_SET
            }

            client.requestGameMode(size, size, gameMode, stones)
        }
    }

    // TODO: support a light dialog theme variant?
    override fun getTheme() = R.style.Theme_Freebloks_Dialog_MinWidth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN or SOFT_INPUT_STATE_HIDDEN)
            } else {
                window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN or SOFT_INPUT_STATE_HIDDEN)
            }

            val client = client
            if (client != null && client.game.isStarted) {
                /* chat */
                setTitle(R.string.chat)
                setCanceledOnTouchOutside(true)
            } else {
                /* lobby */
                setTitle(R.string.lobby_waiting_for_players)
                setCanceledOnTouchOutside(false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val client = client ?: return

        colorAdapter = ColorAdapter(this, requireContext(), client.game, null)
        color_grid.apply {
            adapter = colorAdapter
            onItemClickListener = this@LobbyDialog
        }

        game_mode.apply {
            setSelection(GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal)
            isEnabled = false
            onItemSelectedListener = gameModeSelectedListener
        }

        field_size.apply {
            setSelection(4)
            isEnabled = false
            onItemSelectedListener = sizeSelectedListener
        }

        startButton.setOnClickListener { client.requestGameStart() }

        chatText.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    chatButton.isEnabled = s.isNotEmpty()
                }
            })
            setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_NULL) {
                    sendChat()
                    return@OnEditorActionListener true
                }
                false
            })
        }

        chatButton.apply {
            isEnabled = false
            setOnClickListener { sendChat() }
        }

        chatAdapter = ChatListAdapter(requireContext(), client.game.gameMode)
        chatList.adapter = chatAdapter

        viewModel.chatHistoryAsLiveData.observe(viewLifecycleOwner, Observer { chatAdapter?.setData(it) })

        viewModel.connectionStatus.observe(viewLifecycleOwner, Observer { onConnectionStatusChanged(it) })
        if (client.game.isStarted) {
            /* chat */
            startButton.visibility = View.GONE
        } else {
            /* lobby */
            startButton.visibility = View.VISIBLE
        }

        updateViewsFromStatus()
        client.addObserver(this)
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
             * has to be disconnected. just close the lobby since there is no
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

        val dialogView = layoutInflater.inflate(R.layout.edit_name_dialog, null)
        val edit = dialogView.edit.apply {
            setText(lastStatus.getClientName(lastStatus.getClient(player)))
        }

        MaterialAlertDialogBuilder(requireContext()).apply {
            setView(dialogView)
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

    private fun updateViewsFromStatus() {
        /* better: dismiss */
        if (!isVisible) return
        val client = client ?: return
        val status = client.lastStatus

        colorAdapter?.setCurrentStatus(status)

        if (status == null) {
            game_mode.isEnabled = false
            field_size.isEnabled = false
        } else {
            chatAdapter?.gameMode = status.gameMode

            game_mode.setSelection(status.gameMode.ordinal)
            game_mode.isEnabled = !client.game.isStarted

            var slider = 3
            for (i in GameConfig.FIELD_SIZES.indices)
                if (GameConfig.FIELD_SIZES[i] == status.width)
                    slider = i

            field_size.setSelection(slider)
            field_size.isEnabled = !client.game.isStarted
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

    /**
     * Send what ever is currently in the chatText view to the server and reset the edit field
     */
    private fun sendChat() {
        val text = chatText.text.toString()
        if (text.isEmpty()) return
        // FIXME: The server cuts off the last character, so we have to append a new-line
        client?.sendChat(text + "\n")
        chatText.setText("")
    }

    override fun gameStarted() {
        handler.post { dismissAllowingStateLoss() }
    }

    override fun serverStatus(status: MessageServerStatus) {
        handler.post { updateViewsFromStatus() }
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {
        handler.post { updateViewsFromStatus() }
    }

    override fun playerLeft(client: Int, player: Int, name: String?) {
        handler.post { updateViewsFromStatus() }
    }

    private fun onConnectionStatusChanged(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.Disconnected,
            ConnectionStatus.Failed -> dismiss()
        }
    }
}