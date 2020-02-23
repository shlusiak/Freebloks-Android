package de.saschahlusiak.freebloks.lobby

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView.OnEditorActionListener
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.client.GameClient
import de.saschahlusiak.freebloks.client.GameEventObserver
import de.saschahlusiak.freebloks.game.CustomGameDialog
import de.saschahlusiak.freebloks.game.FreebloksActivity
import de.saschahlusiak.freebloks.game.GameConfiguration
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.network.message.MessageServerStatus
import kotlinx.android.synthetic.main.edit_name_dialog.view.*
import kotlinx.android.synthetic.main.lobby_dialog.*

class LobbyDialog(private val activity: FreebloksActivity) : Dialog(activity), GameEventObserver, OnItemClickListener {
    private val viewModel = activity.viewModel

    private val chatAdapter = ChatListAdapter(activity, GameMode.GAMEMODE_4_COLORS_4_PLAYERS)
    private val colorAdapter = ColorAdapter(this, context, null, null)

    private var client: GameClient? = null

    private val chatObserver = Observer<List<ChatEntry>> { chatEntries ->
        chatAdapter.setData(chatEntries)
    }

    private val gameModeSelectedListener = object : OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val client = client ?: return

            val gameMode = from(game_mode.selectedItemPosition)

            val size = when (gameMode) {
                GameMode.GAMEMODE_DUO,
                GameMode.GAMEMODE_JUNIOR -> 14

                GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> 15

                else -> 20
            }

            val stones = when (gameMode) {
                GameMode.GAMEMODE_JUNIOR -> GameConfiguration.JUNIOR_STONE_SET
                else -> GameConfiguration.DEFAULT_STONE_SET
            }

            client.requestGameMode(size, size, gameMode, stones)
        }
    }

    private val sizeSelectedListener = object : OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {}

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val client = client ?: return
            val gameMode = client.game.gameMode

            val size = CustomGameDialog.FIELD_SIZES[field_size.selectedItemPosition]

            val stones = when (gameMode) {
                GameMode.GAMEMODE_JUNIOR -> GameConfiguration.JUNIOR_STONE_SET
                else -> GameConfiguration.DEFAULT_STONE_SET
            }

            client.requestGameMode(size, size, gameMode, stones)
        }
    }

    init {
        setCancelable(true)

        requestWindowFeature(Window.FEATURE_LEFT_ICON)
        setContentView(R.layout.lobby_dialog)
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.notification_waiting_large)

        /* to make sure we have enough real estate. not necessary on xlarge displays */
        if (activity.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK !=
            Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            window?.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE or SOFT_INPUT_STATE_HIDDEN)
        } else {
            window?.setSoftInputMode(SOFT_INPUT_ADJUST_PAN or SOFT_INPUT_STATE_HIDDEN)
        }

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

        startButton.setOnClickListener { client?.requestGameStart() }

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

        chatList.adapter = chatAdapter
    }

    override fun onStart() {
        super.onStart()
        val client = viewModel.client
        if (client == null) {
            dismiss()
            return
        }

        this.client = client

        client.addObserver(this)

        if (client.game.isStarted) {
            /* chat */
            startButton.visibility = View.GONE
            setTitle(R.string.chat)
            setCanceledOnTouchOutside(true)
        } else {
            /* lobby */
            startButton.visibility = View.VISIBLE
            setTitle(R.string.lobby_waiting_for_players)
            setCanceledOnTouchOutside(false)
        }

        chatAdapter.gameMode = client.game.gameMode
        updateViewsFromStatus()
        chatAdapter.notifyDataSetChanged()

        viewModel.chatHistoryAsLiveData.observe(activity, chatObserver)
    }

    override fun onStop() {
        client?.removeObserver(this)
        viewModel.chatHistoryAsLiveData.removeObserver(chatObserver)
        super.onStop()
    }

    fun editPlayerName(player: Int) {
        val lastStatus = client?.lastStatus ?: return

        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.edit_name_dialog, null)
        dialogBuilder.setView(dialogView)
        val edit = dialogView.edit
        edit.setText(lastStatus.getClientName(lastStatus.getClient(player)))
        dialogBuilder.setTitle(R.string.prefs_player_name)
        dialogBuilder.setPositiveButton(android.R.string.ok) { _, _ ->
            val name = edit.text.toString().trim()
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("player_name", name)
                .apply()

            client?.revokePlayer(player)
            client?.requestPlayer(player, name)
        }
        dialogBuilder.setNegativeButton(android.R.string.cancel) { _, _ -> }

        val b = dialogBuilder.create()
        b.show()

        edit.selectAll()
        edit.requestFocus()
        b.window?.clearFlags(FLAG_NOT_FOCUSABLE or FLAG_ALT_FOCUSABLE_IM)
        b.window?.setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    /**
     * Send what ever is currently in the chatText view to the server and reset the edit field
     */
    private fun sendChat() {
        val text = chatText.text.toString()
        if (text.isEmpty()) return
        client?.sendChat(text)
        chatText.setText("")
    }

    override fun gameStarted() {
        dismiss()
    }

    override fun serverStatus(status: MessageServerStatus) {
        chatList.post { updateViewsFromStatus() }
    }

    override fun playerJoined(client: Int, player: Int, name: String?) {
        chatList.post { updateViewsFromStatus() }
    }

    override fun playerLeft(client: Int, player: Int, name: String?) {
        chatList.post { updateViewsFromStatus() }
    }

    override fun onDisconnected(client: GameClient, error: Exception?) {
        dismiss()
    }

    private fun updateViewsFromStatus() {
        /* better: dismiss */
        val client = client ?: return
        val status = client.lastStatus

        colorAdapter.setCurrentStatus(client.game, status)

        if (status == null) {
            clients?.visibility = View.INVISIBLE
            game_mode.isEnabled = false
            field_size.isEnabled = false
        } else {
            chatAdapter.gameMode = status.gameMode

            clients?.visibility = View.VISIBLE
            clients?.text = context.resources.getQuantityString(R.plurals.connected_clients, status.clients, status.clients)

            game_mode.setSelection(status.gameMode.ordinal)
            game_mode.isEnabled = !client.game.isStarted

            var slider = 3
            for (i in CustomGameDialog.FIELD_SIZES.indices)
                if (CustomGameDialog.FIELD_SIZES[i] == status.width)
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
}