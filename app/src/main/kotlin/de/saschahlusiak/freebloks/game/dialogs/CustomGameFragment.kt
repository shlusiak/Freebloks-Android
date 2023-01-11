package de.saschahlusiak.freebloks.game.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.preference.PreferenceManager
import com.shawnlin.numberpicker.NumberPicker
import de.saschahlusiak.freebloks.R
import de.saschahlusiak.freebloks.databinding.CustomGameFragmentBinding
import de.saschahlusiak.freebloks.game.OnStartCustomGameListener
import de.saschahlusiak.freebloks.model.*
import de.saschahlusiak.freebloks.model.GameMode.*
import de.saschahlusiak.freebloks.utils.MaterialDialogFragment
import de.saschahlusiak.freebloks.utils.viewBinding

class CustomGameFragment : MaterialDialogFragment(R.layout.custom_game_fragment), OnSeekBarChangeListener, View.OnClickListener, OnItemSelectedListener {
    // the values of the difficulty slider for each index
    private val difficultyValues = intArrayOf(
        200, 150, 130, 90, 60, 40, 20, 10, 5, 2, 1
    )

    private lateinit var players: Array<CheckBox>
    private lateinit var picker: Array<NumberPicker>
    private val listener get() = requireActivity() as OnStartCustomGameListener

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    private val binding by viewBinding(CustomGameFragmentBinding::bind)

    /**
     * Convenient getter/setter for GameMode
     */
    private var gameMode: GameMode
        get() = GameMode.from(binding.gameModeSpinner.selectedItemPosition)
        set(value) = binding.gameModeSpinner.setSelection(value.ordinal)

    /**
     * Convenient getter/setter for field size
     */
    private var fieldSize: Int
        get() = GameConfig.FIELD_SIZES[binding.fieldSizeSpinner.selectedItemPosition]
        set(value) {
            val selection = GameConfig.FIELD_SIZES.indexOfFirst { it == value }
            if (selection >= 0)
                binding.fieldSizeSpinner.setSelection(selection)
            else
                binding.fieldSizeSpinner.setSelection(4)
        }

    /**
     * Convenient getter for the selected stones
     */
    private val stones: IntArray
        get() = IntArray(Shape.COUNT) {
            picker[Shape.get(it).points - 1].value
        }

    /**
     * Convenient getter for the difficulty slider to value
     */
    private var difficulty: Int
        get() = difficultyValues[binding.difficultySlider.progress]
        set(value) {
            val selection = difficultyValues.indexOfFirst { it == value }
            if (selection >= 0)
                binding.difficultySlider.progress = selection
            else
                // this is really just when the value is not found, which should never happen
                binding.difficultySlider.progress = difficultyValues.size - 1
        }

    private val playersAsBooleanArray get() = BooleanArray(4) {
        /* this would otherwise request players two times, the server would hand out 2x2 = 4 players */
        players[it].isChecked && (gameMode != GAMEMODE_4_COLORS_2_PLAYERS || it < 2)
    }

    override fun getTheme() = R.style.Theme_Freebloks_DayNight_Dialog_MinWidth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        difficultySlider.setOnSeekBarChangeListener(this@CustomGameFragment)
        difficultySlider.max = difficultyValues.size - 1

        players = arrayOf(player1, player2, player3, player4)
        picker = arrayOf(picker1, picker2, picker3, picker4, picker5)

        gameModeSpinner.onItemSelectedListener = this@CustomGameFragment
        advanced.setOnClickListener(this@CustomGameFragment)
        player1.setOnClickListener(this@CustomGameFragment)
        player2.setOnClickListener(this@CustomGameFragment)
        cancel.setOnClickListener(this@CustomGameFragment)
        ok.setOnClickListener(this@CustomGameFragment)

        difficulty = prefs.getInt("difficulty", GameConfig.DEFAULT_DIFFICULTY)
        gameMode = GameMode.from(prefs.getInt("gamemode", GAMEMODE_4_COLORS_4_PLAYERS.ordinal))
        fieldSize = prefs.getInt("fieldsize", Board.DEFAULT_BOARD_SIZE)

        setupDialog()
    }

    private fun setupDialog() {
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

        requireView().visibility = View.VISIBLE
        binding.customStonesLayout.visibility = View.GONE
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
        view?.run {
            when (v.id) {
                R.id.ok -> {
                    saveSettings()

                    // we don't need to request a player name, because we overwrite it locally anyway when displaying
                    listener.onStartClientGameWithConfig(buildGameConfig(), null)
                    dismiss()
                }
                R.id.cancel -> {
                    dismiss()
                }
                R.id.advanced -> with(binding) {
                    advanced.visibility = View.GONE
                    customStonesLayout.visibility = View.VISIBLE
                }
                R.id.player1 -> if (gameMode == GAMEMODE_4_COLORS_2_PLAYERS) with(binding) {
                    player3.isChecked = player1.isChecked
                }
                R.id.player2 -> if (gameMode == GAMEMODE_4_COLORS_2_PLAYERS) with(binding) {
                    player4.isChecked = player2.isChecked
                }
            }
        }
    }

    /**
     * New game mode is selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (gameMode) {
            GAMEMODE_DUO,
            GAMEMODE_JUNIOR  -> {
                players[0].isEnabled = true
                players[1].isEnabled = false
                players[2].isEnabled = true
                players[3].isEnabled = false
                if (players[1].isChecked) players[0].isChecked = true
                if (players[3].isChecked) players[2].isChecked = true
                players[1].isChecked = false
                players[3].isChecked = false
                /* FIXME: on first create this is called after prepare, which does seem to not persist the
                 * last set size if != 14 */
                fieldSize = 14
            }
            GAMEMODE_2_COLORS_2_PLAYERS -> {
                players[0].isEnabled = true
                players[2].isEnabled = true
                players[1].isEnabled = false
                players[3].isEnabled = false
                if (players[1].isChecked) players[0].isChecked = true
                if (players[3].isChecked) players[2].isChecked = true
                players[1].isChecked = false
                players[3].isChecked = false
                /* FIXME: on first create this is called after prepare, which does seem to not persist the
                 * last set size if != 15 */
                fieldSize = 15
            }
            GAMEMODE_4_COLORS_2_PLAYERS -> {
                var e: Boolean
                players[0].isEnabled = true
                players[1].isEnabled = true
                players[2].isEnabled = false
                players[3].isEnabled = false

                e = players[0].isChecked || players[2].isChecked
                players[0].isChecked = e
                players[2].isChecked = e

                e = players[1].isChecked || players[3].isChecked
                players[1].isChecked = e
                players[3].isChecked = e
            }
            GAMEMODE_4_COLORS_4_PLAYERS -> {
                players[0].isEnabled = true
                players[1].isEnabled = true
                players[2].isEnabled = true
                players[3].isEnabled = true
            }
        }

        updateColorNames()
    }

    private fun updateColorNames() {
        players.forEachIndexed { index, checkBox ->
            checkBox.text = gameMode.colorOf(index).getName(requireContext().resources)
        }
    }

    private fun saveSettings() {
        prefs.edit()
            .putInt("difficulty", difficulty)
            .putInt("gamemode", gameMode.ordinal)
            .putInt("fieldsize", fieldSize)
            .apply()
    }

    private fun updateDifficultyLabel() {
        // 5 values, from hardest to easiest
        val labels = requireContext().resources.getStringArray(R.array.difficulties)
        val value = difficulty
        var text = 0
        if (value >= 5) text = 1
        if (value >= 40) text = 2
        if (value >= 80) text = 3
        if (value >= 160) text = 4
        binding.difficultyLabel.text = String.format("%s (%d)", labels[text], binding.difficultySlider.progress)
    }

    override fun onProgressChanged(seekBar: SeekBar, arg1: Int, arg2: Boolean) {
        updateDifficultyLabel()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setTitle(R.string.custom_game)
        }
    }
}