package de.saschahlusiak.freebloks.game.dialogs

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameConfig.Companion.defaultStonesForMode
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.prefs
import kotlinx.android.synthetic.main.color_grid_item.view.*
import kotlinx.android.synthetic.main.color_list_custom_title.*
import kotlinx.android.synthetic.main.color_list_dialog.*

class ColorListDialog : MaterialDialogFragment(), OnItemClickListener, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private var list: AdapterView<ColorListAdapter>? = null
    private var adapter: ColorListAdapter? = null
    private var selection = BooleanArray(4) { false }
    private val listener get() = (requireActivity() as OnStartCustomGameListener)

    override fun getTheme() = R.style.Theme_Freebloks_Light_Dialog_MinWidth

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext(), theme).apply {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.color_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        game_mode.onItemSelectedListener = this
        field_size.onItemSelectedListener = this

        if (savedInstanceState != null) {
            selection = savedInstanceState.getBooleanArray("color_selection") ?: BooleanArray(4) { false }
        }

        adapter = ColorListAdapter()

        // Can't have the same id for list and grid, otherwise rotate on Android 2.3 crashes
        // with class cast exception
        list = view.findViewById(android.R.id.list) ?: view.findViewById(R.id.grid)
        list?.apply {
            adapter = this@ColorListDialog.adapter
            setOnItemClickListener(this@ColorListDialog)
        }

        startButton.setOnClickListener(this)
        pass_and_play.setOnCheckedChangeListener(this)
        adapter?.setPassAndPlay(pass_and_play.isChecked)
        val previousGameMode = from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal))

        // TODO: restore the previous field size; the setGameMode will set the default for the given game mode
//		final int previousFieldSize = prefs.getInt("fieldsize", Board.DEFAULT_BOARD_SIZE);
        setGameMode(previousGameMode)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray("color_selection", selection)
    }

    private fun setGameMode(gameMode: GameMode) {
        game_mode.setSelection(gameMode.ordinal)
        when (gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> {
                field_size.setSelection(2)
                run {
                    selection[3] = false
                    selection[1] = selection[3]
                }
            }
            GameMode.GAMEMODE_DUO, GameMode.GAMEMODE_JUNIOR -> {
                field_size.setSelection(1)
                run {
                    selection[3] = false
                    selection[1] = selection[3]
                }
            }
            GameMode.GAMEMODE_4_COLORS_2_PLAYERS -> {
                field_size.setSelection(4)
                run {
                    selection[3] = false
                    selection[2] = selection[3]
                }
            }
            else -> field_size.setSelection(4)
        }
        adapter?.setGameMode(gameMode)
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (pass_and_play.isChecked) {
            selection[id.toInt()] = !selection[id.toInt()]
            this.adapter?.notifyDataSetChanged()
        } else {
            val players = BooleanArray(4)
            players[id.toInt()] = true
            val config = buildConfiguration(players)
            listener.onStartClientGameWithConfig(config, null)
            dismiss()

            prefs.edit()
                .putInt("gamemode", config.gameMode.ordinal)
                .putInt("fieldsize", config.fieldSize)
                .apply()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (parent === game_mode) setGameMode(from(position))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun buildConfiguration(players: BooleanArray?): GameConfig {
        val mode = from(game_mode.selectedItemPosition)
        val size = GameConfig.FIELD_SIZES[field_size.selectedItemPosition]

        return GameConfig(
            null,
            mode,
            false,
            players,
            GameConfig.DEFAULT_DIFFICULTY,
            defaultStonesForMode(mode),
            size
        )
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        adapter?.setPassAndPlay(pass_and_play.isChecked)
        if (pass_and_play.isChecked) {
            startButton.setText(R.string.start)
        } else {
            startButton.setText(R.string.random_color)
        }
    }

    override fun onClick(v: View) {
        val players = if (pass_and_play.isChecked) selection else null
        val config = buildConfiguration(players)
        listener.onStartClientGameWithConfig(config, null)
        dismiss()

        prefs.edit()
            .putInt("gamemode", config.gameMode.ordinal)
            .putInt("fieldsize", config.fieldSize)
            .apply()
    }

    private inner class ColorListAdapter : ArrayAdapter<String>(requireContext(), R.layout.color_list_item) {
        private val colors = context.resources.getStringArray(R.array.color_names)
        private var gameMode: GameMode? = null
        private var passAndPlay = false

        fun setPassAndPlay(passAndPlay: Boolean) {
            this.passAndPlay = passAndPlay
            notifyDataSetChanged()
        }

        override fun areAllItemsEnabled() = false

        override fun isEnabled(position: Int): Boolean {
            if (passAndPlay && gameMode === GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
                if (getItemId(position) > 1) return false
            }
            return true
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.color_list_item, parent, false)

            view.findViewById<TextView>(android.R.id.text1).apply {
                text = getItem(position)
            }

            val playerColor = Global.getPlayerColor(getItemId(position).toInt(), gameMode ?: GameMode.DEFAULT)
            val ld = ContextCompat.getDrawable(context, R.drawable.bg_card_1) as LayerDrawable
            ld.mutate()

            (ld.findDrawableByLayerId(R.id.shadow) as GradientDrawable).apply {
                val res = Global.PLAYER_BACKGROUND_COLOR_RESOURCE[playerColor]
                setColor(ContextCompat.getColor(context, res))
            }
            (ld.findDrawableByLayerId(R.id.color1) as GradientDrawable).apply {
                val res = Global.PLAYER_FOREGROUND_COLOR_RESOURCE[playerColor]
                setColor(ContextCompat.getColor(context, res))
            }

            view.findViewById<View>(R.id.color).apply {
                background = ld
            }

            val c = view.checkBox
            if (passAndPlay) {
                c.isEnabled = true
                var itemId = getItemId(position).toInt()
                if (gameMode === GameMode.GAMEMODE_4_COLORS_2_PLAYERS) {
                    if (itemId > 1) {
                        itemId -= 2
                        c.isEnabled = false
                    }
                }
                c.visibility = View.VISIBLE
                c.isChecked = selection[itemId]
            } else {
                c.visibility = View.GONE
            }
            return view
        }

        fun setGameMode(gameMode: GameMode) {
            if (gameMode === this.gameMode) return
            clear()

            when (gameMode) {
                GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> {
                    add(colors[1]) // blue
                    add(colors[3]) // red
                }
                GameMode.GAMEMODE_DUO,
                GameMode.GAMEMODE_JUNIOR -> {
                    add(colors[5])
                    add(colors[6])
                }
                GameMode.GAMEMODE_4_COLORS_2_PLAYERS,
                GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> {
                    add(colors[1]) // blue
                    add(colors[2]) // Yellow
                    add(colors[3]) // blue
                    add(colors[4]) // green
                }
            }
            this.gameMode = gameMode
            notifyDataSetChanged()
        }

        override fun getItemId(position: Int): Long {
            return when (gameMode) {
                GameMode.GAMEMODE_2_COLORS_2_PLAYERS,
                GameMode.GAMEMODE_DUO,
                GameMode.GAMEMODE_JUNIOR -> if (position == 0) 0 else 2
                else -> position.toLong()
            }
        }
    }
}