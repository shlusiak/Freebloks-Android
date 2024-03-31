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
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import de.saschahlusiak.freebloks.Feature
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.app.AppTheme
import de.saschahlusiak.freebloks.databinding.ColorListFragmentBinding
import de.saschahlusiak.freebloks.databinding.ColorListItemBinding
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameConfig.Companion.defaultStonesForMode
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.StoneColor
import de.saschahlusiak.freebloks.model.defaultBoardSize
import de.saschahlusiak.freebloks.utils.MaterialDialog
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.prefs
import de.saschahlusiak.freebloks.utils.viewBinding

class ColorListFragment : MaterialDialogFragment(R.layout.color_list_fragment), OnItemClickListener, OnItemSelectedListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private var adapter: ColorListAdapter? = null
    private var selection = BooleanArray(4) { false }
    private val listener get() = (requireActivity() as OnStartCustomGameListener)
    private val binding by viewBinding(ColorListFragmentBinding::bind)

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    private val listView get() = binding.list ?: binding.grid

    override fun onCreateDialog(savedInstanceState: Bundle?): MaterialDialog {
        return MaterialDialog(requireContext(), theme, !Feature.COMPOSE).apply {
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (Feature.COMPOSE)
            ComposeView(requireContext())
        else super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val previousGameMode = from(prefs.getInt("gamemode", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.ordinal))
        val previousSize = prefs.getInt("fieldsize", GameMode.GAMEMODE_4_COLORS_4_PLAYERS.defaultBoardSize())

        if (view is ComposeView) {
            dialog?.window?.setBackgroundDrawable(null)
            view.setContent {
                Content(previousGameMode, previousSize)
            }
            return
        }

        with(binding) {
            title.gameMode.onItemSelectedListener = this@ColorListFragment
            title.fieldSize.onItemSelectedListener = this@ColorListFragment

            if (savedInstanceState != null) {
                selection = savedInstanceState.getBooleanArray("color_selection")
                    ?: BooleanArray(4) { false }
            }

            adapter = ColorListAdapter()

            // Can't have the same id for list and grid, otherwise rotate on Android 2.3 crashes
            // with class cast exception
            listView?.apply {
                adapter = this@ColorListFragment.adapter
                onItemClickListener = this@ColorListFragment
            }

            startButton.setOnClickListener(this@ColorListFragment)
            title.passAndPlay.setOnCheckedChangeListener(this@ColorListFragment)
            adapter?.setPassAndPlay(title.passAndPlay.isChecked)

            // TODO: restore the previous field size; the setGameMode will set the default for the given game mode
            //		final int previousFieldSize = prefs.getInt("fieldsize", Board.DEFAULT_BOARD_SIZE);
            setGameMode(previousGameMode)
            Unit
        }
    }

    @Composable
    private fun Content(gameMode: GameMode, size: Int) {
        AppTheme {
            ColorListContent(gameMode, size) { gameMode, size, players ->
                val config = buildConfiguration(players, gameMode, size)
                listener.onStartClientGameWithConfig(config, null)
                dismiss()

                prefs.edit()
                    .putInt("gamemode", config.gameMode.ordinal)
                    .putInt("fieldsize", config.fieldSize)
                    .apply()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray("color_selection", selection)
    }

    private fun setGameMode(gameMode: GameMode) = with(binding.title) {
        this.gameMode.setSelection(gameMode.ordinal)
        when (gameMode) {
            GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> {
                fieldSize.setSelection(2)
                selection[3] = false
                selection[1] = selection[3]
            }
            GameMode.GAMEMODE_DUO, GameMode.GAMEMODE_JUNIOR -> {
                fieldSize.setSelection(1)
                selection[3] = false
                selection[1] = selection[3]
            }
            GameMode.GAMEMODE_4_COLORS_2_PLAYERS -> {
                fieldSize.setSelection(4)
                selection[3] = false
                selection[2] = selection[3]
            }
            else -> fieldSize.setSelection(4)
        }
        adapter?.setGameMode(gameMode)
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (binding.title.passAndPlay.isChecked) {
            selection[id.toInt()] = !selection[id.toInt()]
            this.adapter?.notifyDataSetChanged()
        } else {
            val players = BooleanArray(4)
            players[id.toInt()] = true
            val config = buildConfiguration(
                players = players,
                mode = from(binding.title.gameMode.selectedItemPosition),
                size = GameConfig.FIELD_SIZES[binding.title.fieldSize.selectedItemPosition]
            )
            listener.onStartClientGameWithConfig(config, null)
            dismiss()

            prefs.edit()
                .putInt("gamemode", config.gameMode.ordinal)
                .putInt("fieldsize", config.fieldSize)
                .apply()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        if (parent === binding.title.gameMode) setGameMode(from(position))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    private fun buildConfiguration(players: BooleanArray?, mode: GameMode, size: Int): GameConfig {
        return GameConfig(
            isLocal = true,
            server = null,
            gameMode = mode,
            showLobby = false,
            requestPlayers = players,
            difficulty = GameConfig.DEFAULT_DIFFICULTY,
            stones = defaultStonesForMode(mode),
            fieldSize = size
        )
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) = with(binding) {
        adapter?.setPassAndPlay(title.passAndPlay.isChecked)
        if (title.passAndPlay.isChecked) {
            startButton.setText(R.string.start)
        } else {
            startButton.setText(R.string.random_color)
        }
    }

    override fun onClick(v: View) {
        val players = if (binding.title.passAndPlay.isChecked) selection else null
        val config = buildConfiguration(
            players = players,
            mode = from(binding.title.gameMode.selectedItemPosition),
            size = GameConfig.FIELD_SIZES[binding.title.fieldSize.selectedItemPosition]
        )
        listener.onStartClientGameWithConfig(config, null)
        dismiss()

        prefs.edit()
            .putInt("gamemode", config.gameMode.ordinal)
            .putInt("fieldsize", config.fieldSize)
            .apply()
    }

    private inner class ColorListAdapter : ArrayAdapter<StoneColor>(requireContext(), R.layout.color_list_item) {
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
            val view = convertView
                ?: layoutInflater.inflate(R.layout.color_list_item, parent, false)
            val binding = ColorListItemBinding.bind(view)

            binding.text1.apply {
                text = getItem(position)?.getName(resources)
            }

            val playerColor = getItem(position) ?: return view
            val ld = ContextCompat.getDrawable(context, R.drawable.bg_card_1) as LayerDrawable
            ld.mutate()

            (ld.findDrawableByLayerId(R.id.shadow) as GradientDrawable).apply {
                val res = playerColor.backgroundColorId
                setColor(ContextCompat.getColor(context, res))
            }
            (ld.findDrawableByLayerId(R.id.color1) as GradientDrawable).apply {
                val res = playerColor.foregroundColorId
                setColor(ContextCompat.getColor(context, res))
            }

            binding.color.apply {
                background = ld
            }

            val c = binding.checkBox
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
                GameMode.GAMEMODE_2_COLORS_2_PLAYERS -> addAll(
                    StoneColor.Blue,
                    StoneColor.Red
                )
                GameMode.GAMEMODE_DUO,
                GameMode.GAMEMODE_JUNIOR -> addAll(
                    StoneColor.Orange,
                    StoneColor.Purple
                )
                GameMode.GAMEMODE_4_COLORS_2_PLAYERS,
                GameMode.GAMEMODE_4_COLORS_4_PLAYERS -> addAll(
                    StoneColor.Blue,
                    StoneColor.Yellow,
                    StoneColor.Red,
                    StoneColor.Green
                )
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