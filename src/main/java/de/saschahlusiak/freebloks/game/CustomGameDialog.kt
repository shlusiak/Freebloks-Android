package de.saschahlusiak.freebloks.game

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.preference.PreferenceManager
import com.shawnlin.numberpicker.NumberPicker
import de.saschahlusiak.freebloks.Global
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.model.GameConfig
import de.saschahlusiak.freebloks.model.GameMode
import de.saschahlusiak.freebloks.model.GameMode.*
import de.saschahlusiak.freebloks.model.GameMode.Companion.from
import de.saschahlusiak.freebloks.model.Shape
import de.saschahlusiak.freebloks.model.Shape.Companion.get
import kotlinx.android.synthetic.main.custom_game_dialog.*

class CustomGameDialog(context: Context, private val listener: OnStartCustomGameListener) : Dialog(context, R.style.Theme_Freebloks_Light_Dialog), OnSeekBarChangeListener, View.OnClickListener, OnItemSelectedListener {
    // the values of the difficulty slider for each index
    private val DIFFICULTY_VALUES = intArrayOf(
        200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
    )
    // highest possible index
    private val DIFFICULTY_MAX = 10 /* 0..10 = 11 */
    // default index
    private val DIFFICULTY_DEFAULT = 8

    private lateinit var players: Array<CheckBox>
    private lateinit var picker: Array<NumberPicker>

    /**
     * Convenient getter/setter for GameMode
     */
    private var gameMode: GameMode
        get() = from(game_mode.selectedItemPosition)
        set(value) = game_mode.setSelection(value.ordinal)

    /**
     * Convenient getter/setter for field size
     */
    private var fieldSize: Int
        get() = GameConfig.FIELD_SIZES[field_size.selectedItemPosition]
        set(value) {
            val selection = GameConfig.FIELD_SIZES.indexOfFirst { it == value }
            if (selection >= 0)
                field_size.setSelection(selection)
            else
                field_size.setSelection(4)
        }

    /**
     * Convenient getter for the selected stones
     */
    private val stones: IntArray
        get() = IntArray(Shape.COUNT) {
            picker[get(it).points - 1].value
        }

    private var difficulty: Int
        get() = DIFFICULTY_VALUES[difficulty_slider.progress]
        set(value) {
            val selection = DIFFICULTY_VALUES.indexOfFirst { it == value }
            if (selection >= 0)
                this.difficulty_slider.progress = selection
            else
                this.difficulty_slider.progress = DIFFICULTY_DEFAULT
        }

    private val playersAsBooleanArray get() = BooleanArray(4) {
        /* this would otherwise request players two times, the server would hand out 2x2 = 4 players */
        players[it].isChecked && (gameMode != GAMEMODE_4_COLORS_2_PLAYERS || it < 2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.custom_game_dialog)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        setTitle(R.string.custom_game_title)

        difficulty_slider.setOnSeekBarChangeListener(this)
        difficulty_slider.max = DIFFICULTY_MAX

        players = arrayOf(player1, player2, player3, player4)
        picker = arrayOf(picker1, picker2, picker3, picker4, picker5)

        game_mode.onItemSelectedListener = this
        advanced.setOnClickListener(this)
        player1.setOnClickListener(this)
        player2.setOnClickListener(this)
        cancel.setOnClickListener(this)
        ok.setOnClickListener(this)

        updateColorNames()
        updateDifficultyLabel()
    }


    fun setupDialog(difficulty: Int, gameMode: GameMode, fieldSize: Int) {
        this.gameMode = gameMode
        this.fieldSize = fieldSize
        this.difficulty = difficulty

        players.forEach { it.isChecked = false }
        val p = when(gameMode) {
            GAMEMODE_2_COLORS_2_PLAYERS -> (Math.random() * 2.0).toInt() * 2
            GAMEMODE_DUO,
            GAMEMODE_JUNIOR -> (Math.random() * 2.0).toInt() * 2
            GAMEMODE_4_COLORS_2_PLAYERS -> (Math.random() * 2.0).toInt()
            GAMEMODE_4_COLORS_4_PLAYERS -> (Math.random() * 4.0).toInt()
        }
        players[p].isChecked = true

        if (gameMode === GAMEMODE_4_COLORS_2_PLAYERS) {
            players[2].isChecked = players[0].isChecked
            players[3].isChecked = players[1].isChecked
        }

        updateColorNames()
        updateDifficultyLabel()

        advanced.visibility = View.VISIBLE
        custom_stones_layout.visibility = View.GONE
    }

    private fun buildGameConfig() = GameConfig(
        null,
        gameMode,
        false,
        playersAsBooleanArray,
        difficulty,
        stones,
        fieldSize
    )

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ok -> {
                saveSettings()

                listener.onStartClientGameWithConfig(buildGameConfig())
                dismiss()
            }
            R.id.cancel -> {
                dismiss()
            }
            R.id.advanced -> {
                advanced.visibility = View.GONE
                custom_stones_layout.visibility = View.VISIBLE
            }
            R.id.player1 -> if (gameMode == GAMEMODE_4_COLORS_2_PLAYERS) {
                player3.isChecked = player1.isChecked
            }
            R.id.player2 -> if (gameMode == GAMEMODE_4_COLORS_2_PLAYERS) {
                player4.isChecked = player2.isChecked
            }
        }
    }

    /**
     * New game mode is selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        when (gameMode) {
            GAMEMODE_DUO,
            GAMEMODE_JUNIOR  -> {
                player1.isEnabled = true
                player2.isEnabled = false
                player3.isEnabled = true
                player4.isEnabled = false
                if (player2.isChecked) player1.isChecked = true
                if (player4.isChecked) player3.isChecked = true
                player2.isChecked = false
                player4.isChecked = false
                /* FIXME: on first create this is called after prepare, which does seem to not persist the
                 * last set size if != 14 */
                fieldSize = 14
            }
            GAMEMODE_2_COLORS_2_PLAYERS -> {
                player1.isEnabled = true
                player3.isEnabled = true
                player2.isEnabled = false
                player4.isEnabled = false
                if (player2.isChecked) player1.isChecked = true
                if (player4.isChecked) player3.isChecked = true
                player2.isChecked = false
                player4.isChecked = false
                /* FIXME: on first create this is called after prepare, which does seem to not persist the
                 * last set size if != 15 */
                fieldSize = 15
            }
            GAMEMODE_4_COLORS_2_PLAYERS -> {
                var e: Boolean
                player1.isEnabled = true
                player2.isEnabled = true
                player3.isEnabled = false
                player4.isEnabled = false

                e = player1.isChecked || player3.isChecked
                player1.isChecked = e
                player3.isChecked = e

                e = player2.isChecked || player4.isChecked
                player2.isChecked = e
                player4.isChecked = e
            }
            GAMEMODE_4_COLORS_4_PLAYERS -> {
                player1.isEnabled = true
                player2.isEnabled = true
                player3.isEnabled = true
                player4.isEnabled = true
            }
        }

        updateColorNames()
    }

    private fun updateColorNames() {
        players.forEachIndexed { index, checkBox ->
            checkBox.text = Global.getColorName(context, index, gameMode)
        }
    }

    private fun saveSettings() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit()
            .putInt("difficulty", difficulty)
            .putInt("gamemode", gameMode.ordinal)
            .putInt("fieldsize", fieldSize)
            .apply()
    }

    private fun updateDifficultyLabel() {
        val labels = context.resources.getStringArray(R.array.difficulties)
        val value = difficulty
        var text = 0
        if (value >= 5) text = 1
        if (value >= 40) text = 2
        if (value >= 80) text = 3
        if (value >= 160) text = 4
        difficulty_label.text = String.format("%s (%d)", labels[text], difficulty_slider.progress)
    }

    override fun onProgressChanged(arg0: SeekBar, arg1: Int, arg2: Boolean) {
        updateDifficultyLabel()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}